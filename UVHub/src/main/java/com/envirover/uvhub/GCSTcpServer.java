/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.envirover.uvhub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.MAVLink.MAVLinkPacket;
import com.envirover.mavlink.MAVLinkChannel;
import com.envirover.mavlink.MAVLinkSocket;
import com.envirover.uvnet.shadow.UVLogbook;
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
    protected final UVLogbook logbook;
    
    private ServerSocket serverSocket;
    private Thread listenerThread;


    /**
     * Creates an instance of GCSTcpServer 
     * 
     * @param port TCP port used for MAVLink ground control stations connections 
     * @param mtMessageQueue Mobile-terminated messages queue
     */
    public GCSTcpServer(Integer port, MAVLinkChannel mtMessageQueue, UVShadow shadow, UVLogbook logbook) {
        this.port = port;
        this.mtMessageQueue = mtMessageQueue;
        this.threadPool = Executors.newCachedThreadPool();
        this.shadow = shadow;
        this.logbook = logbook;
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
                    } catch (SocketException e) {
                        // Connection reset
                        logger.warn(e.getMessage());
                        return;
                    } catch (IOException e) {
                        logger.debug(e.getMessage());
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
    }

}
