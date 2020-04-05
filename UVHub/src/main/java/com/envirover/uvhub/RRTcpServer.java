/*
 * Envirover confidential
 * 
 *  [2020] Envirover
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkSocket;

/**
 * TCP server that accepts TCP/IP connections from UV RadioRoom clients.
 *
 * Only one client connection is open at a time. When new client is connected,
 * the previous one is closed.
 *  
 * @author Pavel Bobov
 *
 */
public class RRTcpServer {

    public final static String CHANNEL_NAME = "tcp";

    private final static Logger logger = LogManager.getLogger(RRTcpServer.class);

    private final Integer port;  // server's  port
    private final MOMessageHandler handler;
    private final Thread connectionListenerThread;
    private final Thread socketListenerThread;

    private ServerSocket serverSocket = null;
    private MAVLinkSocket clientSocket = null;

    /**
     * Creates an instance of RRTcpServer.
     * 
     * @param port TCP port used for SPL RadioRoom connections 
     * @param handler Mobile-originated message handler
     */
    public RRTcpServer(Integer port, MOMessageHandler handler) {
        this.port = port;
        this.handler = handler;
        this.socketListenerThread = new Thread(new SocketListener());
        this.connectionListenerThread = new Thread(new ConnectionListener());
    }

    /**
     * Returns MAVLinkSocket for the last connected client or null if clients
     * were not connected.
     *
     * @return  MAVLinkSocket for the last connected RadioRoom client.
     */
    public synchronized MAVLinkSocket getClientSocket() {
        return clientSocket;
    }

    /**
     * Closes MAVLinkSocket of the previously connected client, if any,
     * and replaces it by the specified MAVLinkSocket.
     *
     * @param clientSocket MAVLinkSocket of the connected client.
     */
    private synchronized void setClientSocket(MAVLinkSocket clientSocket) {
        if (this.clientSocket != null) {
            this.clientSocket.close();
        }

        this.clientSocket = clientSocket;
    }

    /**
     * Starts the server.
     *
     * Creates a server socket, bound to the specified port.
     * Starts the connection listener and socket listener threads.
     * 
     * @throws IOException if binding to the port failed.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        connectionListenerThread.start();
        socketListenerThread.start();
    }

    /**
     * Stops RRTcpServer.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void stop() throws IOException {
        socketListenerThread.interrupt();
        connectionListenerThread.interrupt();

        if (serverSocket != null) {
            serverSocket.close();
            serverSocket = null;
        }

        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
    }

    /**
     * Accepts socket connections. 
     */
    class ConnectionListener implements Runnable {
    	
        @Override
        public void run() {
            while (serverSocket.isBound()) {
                try {
                    Socket socket = serverSocket.accept();

                    // Save the last socket connection. It will be used to as the
                    // primary channel for mobile-terminated messages.
                   	setClientSocket(new MAVLinkSocket(socket));
                    
                    logger.info(MessageFormat.format("RadioRoom client ''{0}'' connected.",
                                                     socket.getInetAddress()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * Reads MAVLink messages from the client socket and passes them to
     * the mobile-originated message handler.
     */
     class SocketListener implements Runnable {

        @Override
        public void run() {
            while (serverSocket.isBound()) {
                try {
                    MAVLinkSocket mavSocket = getClientSocket();

                    if (mavSocket != null) {
                        MAVLinkPacket packet = mavSocket.receiveMessage();

                        if (packet != null) {
                            handler.handleMessage(packet, CHANNEL_NAME);
                        }
                    }

                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
