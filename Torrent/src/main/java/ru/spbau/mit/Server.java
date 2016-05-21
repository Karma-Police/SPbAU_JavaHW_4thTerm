package ru.spbau.mit;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;

    public Server(int port) {
        this.port = port;
    }

    public synchronized void run(HandlerFactory handlerFactory) {
        if (isRunning) {
            logger.log(Level.WARNING, "Failed to run server cause it is already running!");
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            logger.log(Level.INFO, "Created server socket");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create server socket : " + e.toString());
            return;
        }
        isRunning = true;
        executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            while (isRunning) {
                try {
                    executorService.execute(handlerFactory.createHandler(serverSocket.accept()));
                } catch (IOException e) {
                    synchronized (this) {
                        if (!serverSocket.isClosed()) {
                            logger.log(Level.WARNING, "Got an exception while accepting connection : " + e.toString());
                        }
                    }
                } catch (SeederDescription.SeederCreatingException exc) {
                    logger.log(Level.WARNING, "Can't create seeder : " + exc.toString());
                }
            }
        });
    }

    public void stop() {
        synchronized (this) {
            if (!isRunning) {
                logger.log(Level.INFO, "Failed to stop serverSocket cause it is not running..");
                return;
            }
            try {
                isRunning = false;
                serverSocket.close();
                logger.log(Level.INFO, "Closed serverSocket!");
            } catch (IOException e) {
                logger.log(Level.WARNING, "got an exception while closing serverSocket : " + e.toString());
                return;
            }
            executorService.shutdown();
            logger.log(Level.INFO, "Server has been stopped!");
        }
    }

    public synchronized int getPort() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return 0;
        }
        return serverSocket.getLocalPort();
    }
}
