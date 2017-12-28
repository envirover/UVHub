package com.envirover.nvi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkSocket;

/**
 * MAVLink TCP server that accepts TCP/IP connections from NVI RadioRoom clients.
 * {@link com.envirover.nvi.RRClientSession} is created for each client connection. 
 *  
 * @author Pavel Bobov
 *
 */
public class RRTcpServer {

    private final static Logger logger = Logger.getLogger(RRTcpServer.class);

    private final Integer port;
    private final MAVLinkChannel dst;
    private final MAVLinkChannel mtMessageQueue;
    private final ExecutorService threadPool; 
    private ServerSocket serverSocket;
    private Thread listenerThread;

    /**
     * Creates an instance of RRTcpServer 
     * 
     * @param port TCP port used for RadioRoom connections 
     * @param dst MO message handler
     */
    public RRTcpServer(Integer port, MAVLinkChannel dst, MAVLinkChannel mtMessageQueue) {
        this.port = port;
        this.dst = dst;
        this.mtMessageQueue = mtMessageQueue;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Starts RRTcpServer.
     * 
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        listenerThread = new Thread(new ConnectionListener());
        listenerThread.start();
    }

    /**
     * Stops RRTcpServer.
     * 
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void stop() throws IOException {
        threadPool.shutdownNow();
        listenerThread.interrupt();
        serverSocket.close();
    }

    /**
     * Accepts socket connections. 
     * 
     * @author pavel
     *
     */
    class ConnectionListener implements Runnable {

        @Override
        public void run() {
            while (serverSocket.isBound()) {
                try {
                    Socket socket = serverSocket.accept();

                    MAVLinkSocket clientSocket = new MAVLinkSocket(socket);
                    RRClientSession session = new RRClientSession(clientSocket, dst, mtMessageQueue);
                    session.onOpen();

                    threadPool.execute(new SocketListener(clientSocket, session));

                    logger.info(MessageFormat.format("MAVLink client ''{0}'' connected.", socket.getInetAddress()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        /**
         * Reads MAVLink messages from the socket and passes them to RRClientSession.onMessage(). 
         * 
         * @author pavel
         *
         */
        class SocketListener implements Runnable {

            private final MAVLinkSocket clientSocket;
            private final RRClientSession session;

            public SocketListener(MAVLinkSocket clientSocket, RRClientSession session) {
                this.clientSocket = clientSocket;
                this.session = session;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        MAVLinkPacket packet = clientSocket.receiveMessage();

                        if (packet != null) {
                            session.onMessage(packet);
                        }

                        Thread.sleep(10);
                    } catch (InterruptedException | IOException e) {
                        try {
                            session.onClose();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                        logger.info("MAVLink client disconnected.");

                        return;
                    }
                }
            }
        }
    }

}
