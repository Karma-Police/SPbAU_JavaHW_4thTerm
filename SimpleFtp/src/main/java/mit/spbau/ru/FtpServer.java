package mit.spbau.ru;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by michael on 15.03.16.
 */

public class FtpServer {
    public static final int BUFFER_SIZE = 4096;

    private final int port;
    private final ExecutorService threadPool;
    private final Thread listener;
    private ServerSocket serverSocket;
    private volatile boolean isAlive;

    public FtpServer(int port) {
        this.port = port;
        isAlive = false;
        listener = new Thread(new Listener());
        threadPool = Executors.newCachedThreadPool();
        System.err.println("Server has been created");
    }

    public void start() {
        isAlive = true;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        listener.start();
        System.err.println("Server started");
    }

    public void stop() {
        try {
            System.err.println("Server stopping");
            isAlive = false;
            serverSocket.close(); // causes SocketException if Listener is waiting on accept()
            System.err.println("serverSocket closed");
            listener.join();
            System.err.println("listener joined");
            threadPool.shutdown();
            while (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("Awaiting clients to join");
            }
            System.err.println("Server stopped");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                while (isAlive) {
                    try {
                        Socket client = serverSocket.accept();
                        threadPool.submit(new ClientHandler(client));
                    } catch (SocketException exc) {
                        if (isAlive) {
                            exc.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        private void processList(String path, DataOutputStream outputStream) {
            try {
                File file = new File(path);
                if (!file.exists() || !file.isDirectory()) {
                    outputStream.writeInt(0);
                    return;
                }
                File[] files = file.listFiles();
                if (files == null) {
                    outputStream.writeInt(0);
                    return;
                }
                outputStream.writeInt(files.length);
                for (File nextFile : files) {
                    outputStream.writeUTF(nextFile.getName());
                    outputStream.writeUTF(Boolean.toString(nextFile.isDirectory()));
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        private void processFile(String path, DataOutputStream outputStream) {
            try {
                File file = new File(path);
                if (!file.exists() || file.isDirectory()) {
                    outputStream.writeLong(0);
                    return;
                }
                long packetsCount = (file.length() - 1) / BUFFER_SIZE + 1;
                outputStream.writeLong(packetsCount);
                InputStream in = new FileInputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
    }

        @Override
        public void run() {
            try {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                while (isAlive) {
                    try {
                        while (isAlive && inputStream.available() == 0) {
                            Thread.sleep(100);
                        }
                        if (isAlive) {
                            int request = inputStream.readInt();
                            String path = inputStream.readUTF();
                            if (request == 1) {
                                processList(path, outputStream);
                            } else {
                                processFile(path, outputStream);
                            }
                        }
                    } catch (InterruptedException exc) {
                        exc.printStackTrace();
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

