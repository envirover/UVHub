/*
 * Envirover confidential
 * 
 *  [2018] Envirover
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains the property of 
 * Envirover and its suppliers, if any.  The intellectual and technical concepts
 * contained herein are proprietary to Envirover and its suppliers and may be 
 * covered by U.S. and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Envirover.
 */

package com.envirover.uvhub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.uvnet.shadow.UVShadow;

/**
 * MAVLink TCP server that accepts connections from TCP GCS clients.
 * {@link com.envirover.uvhub.GCSClientSession} is created for each client connection. 
 *  
 * @author Pavel Bobov
 *
 */
public class GCSTcpServer {

    private final static Logger logger = LogManager.getLogger(GCSTcpServer.class);

    private final Integer port;
    private final MAVLinkChannel mtMessageQueue;
    private final ExecutorService threadPool; 
    protected final UVShadow shadow;
    
    private ServerSocket serverSocket;
    private Thread listenerThread;


    /**
     * Creates an instance of GCSTcpServer 
     * 
     * @param port TCP port used for MAVLink ground control stations connections 
     * @param mtMessageQueue Mobile-terminated messages queue
     */
    public GCSTcpServer(Integer port, MAVLinkChannel mtMessageQueue, UVShadow shadow) {
        this.port = port;
        this.mtMessageQueue = mtMessageQueue;
        this.threadPool = Executors.newCachedThreadPool();
        this.shadow = shadow;
    }

    /**
     * Starts GCSTcpServer.
     * 
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        listenerThread = new Thread(new ConnectionListener());
        listenerThread.start();
    }

    /**
     * Stops GCSTcpServer.
     * 
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void stop() throws IOException {
        threadPool.shutdownNow();
        listenerThread.interrupt();
        serverSocket.close();
    }

    protected ClientSession createClientSession(MAVLinkSocket clientSocket) {
        return new GCSClientSession(clientSocket, mtMessageQueue, shadow);
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
                    ClientSession session = createClientSession(clientSocket);
                    session.onOpen();

                    threadPool.execute(new SocketListener(clientSocket, session));

                    logger.info(MessageFormat.format("GCS client ''{0}'' connected.", socket.getInetAddress()));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    return;
                }
            }
        }

        /**
         * Reads MAVLink messages from the socket and passes them to GCSClientSession.onMessage(). 
         * 
         * @author pavel
         *
         */
        class SocketListener implements Runnable {

            private final MAVLinkSocket clientSocket;
            private final ClientSession session;

            public SocketListener(MAVLinkSocket clientSocket, ClientSession session) {
                this.clientSocket = clientSocket;
                this.session = session;
            }

            @Override
            public void run() {
                while (session.isOpen()) {
                    try {
                        MAVLinkPacket packet = clientSocket.receiveMessage();

                        if (packet != null) {
                            session.onMessage(packet);
                        }

                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.info(e.getMessage()); 
                        try {
                            session.onClose();
                        } catch (IOException e1) {
                            logger.debug(e1.getMessage());
                        }
                        return;
                    } catch(IOException e) {
                        logger.warn(e.getMessage()); 
                    }
                }
            }
        }
    }

}
