package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 16.05.16.
 */

public abstract class SocketHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(SocketHandler.class.getName());
    protected final Socket socket;
    protected final String socketInfo;

    public SocketHandler(Socket socket) {
        this.socket = socket;
        socketInfo = socket.getInetAddress().toString();
    }

    @Override
    public void run() {
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                handleRequest(inputStream, outputStream);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Got exception : " + e.toString() + ";  from " + socketInfo);
        }
    }

    protected abstract void handleRequest(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException, SeederDescription.SeederCreatingException;
}
