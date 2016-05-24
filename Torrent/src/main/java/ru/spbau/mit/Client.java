package ru.spbau.mit;

import ru.spbau.mit.P2P.FileStatus;
import ru.spbau.mit.P2P.P2PHandler;
import ru.spbau.mit.P2P.P2PServer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 21.05.16.
 */

public class Client {
    public final List<FileEntry> serverFiles = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final String serverAddress;
    private final int serverPort;
    private final Timer timer;
    private final Lock lock;
    private final ExecutorService executorService;

    private boolean isRunning;
    private P2PServer p2pServer;
    private Socket socket;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        executorService = Executors.newFixedThreadPool(10);
        lock = new ReentrantLock();
        timer = new Timer();
    }

    public void run() {
        lock.lock();
        if (isRunning) {
            logger.log(Level.WARNING, "Can't run client because it is already running!");
            lock.unlock();
            return;
        }
        try {
            socket = new Socket(serverAddress, serverPort);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can't connect to the server : " + e.toString());
            lock.unlock();
            return;
        }
        logger.log(Level.FINE, "running p2p server...");
        p2pServer = new P2PServer(0); // 0 means that serverSocket will generate free port by itself
        p2pServer.run(P2PHandler::new);
        logger.log(Level.FINE, "Run p2p server : done");
        timer.schedule(new UpdateTask(), 1000);
        isRunning = true;
        logger.log(Level.FINE, "Run client : done");
        lock.unlock();
    }

    public void stop() {
        lock.lock();
        if (!isRunning) {
            logger.log(Level.WARNING, "Can't close not running client");
            lock.unlock();
            return;
        }
        timer.cancel();
        executorService.shutdown();
        p2pServer.stop();
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Got an exception while closing the socket : " + e.toString());
        }
        isRunning = false;
        logger.log(Level.FINER, "Client was stopped");
        lock.unlock();
    }

    public void upload(Path path) {
        lock.lock();
        if (!isRunning) {
            logger.log(Level.WARNING, "can't upload file cause the server is not running");
            lock.unlock();
            return;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            String fileName = path.getFileName().toString();
            long fileSize = new File(path.toUri()).length();
            outputStream.writeByte(2);
            outputStream.writeUTF(fileName);
            outputStream.writeLong(fileSize);
            outputStream.flush();
            int fileId = inputStream.readInt();
            FileStatus fileStatus = new FileStatus(fileId, fileSize, fileName, true, path);
            P2PServer.myFiles.put(fileId, fileStatus);
        } catch (IOException exc) {
            logger.log(Level.WARNING, "Cant upload file with path = \"" + path.toString() + "\"! Got an exception : " + exc.toString());
        }
        lock.unlock();
        timer.schedule(new UpdateTask(false), 0);
    }

    public void list() {
        lock.lock();
        try {
            logger.log(Level.FINE, "Starting sending list request!");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            outputStream.writeByte(1);
            outputStream.flush();
            serverFiles.clear();
            int cnt = inputStream.readInt();
            while (cnt > 0) {
                --cnt;
                int fileID = inputStream.readInt();
                String fileName = inputStream.readUTF();
                long fileSize = inputStream.readLong();
                serverFiles.add(new FileEntry(fileID, fileName, fileSize));
            }
            logger.log(Level.FINE, "Sending list request... : done");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can't get server files : " + e.toString());
        }
        lock.unlock();
    }

    public List<SeederDescription> getSeeders(int id) {
        lock.lock();
        if (!isRunning) {
            logger.log(Level.WARNING, "can't get seeders because the server is not running");
            lock.unlock();
            return null;
        }
        try {
            logger.log(Level.FINE, "Getting seeders...");
            List<SeederDescription> result = new ArrayList<>();
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            outputStream.writeByte(3);
            outputStream.writeInt(id);
            outputStream.flush();
            int size = inputStream.readInt();
            while (size > 0) {
                --size;
                byte[] ip = new byte[4];
                for (int i = 0; i < 4; i++) {
                    ip[i] = inputStream.readByte();
                }
                int port = inputStream.readInt();
                SeederDescription nextSD = new SeederDescription(ip);
                nextSD.setPort(port);
                result.add(nextSD);
            }
            logger.log(Level.FINE, "Getting seeders... done");
            lock.unlock();
            return result;
        } catch (IOException exc) {
            logger.log(Level.WARNING, "Cant get seeders of file " + id + "! Got an exception : " + exc.toString());
        }
        lock.unlock();
        return null;
    }

    public boolean checkSeederContainsPart(SeederDescription seeder, int fileID, int part) {
        logger.log(Level.FINE, "Going to check seeder for a part. Seeder ip = " + (int) seeder.ip[0] + "."
                + (int) seeder.ip[1] + "." + (int) seeder.ip[2] + "." + (int) seeder.ip[3]
                + ", fileID = " + fileID + ", file part = " + part + ", port = " + seeder.getPort());
        try(Socket socket = new Socket(InetAddress.getByAddress(seeder.ip), seeder.getPort())) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            outputStream.writeByte(1);
            outputStream.writeInt(fileID);
            outputStream.flush();
            int count = inputStream.readInt();
            boolean result = false;
            while (count > 0) {
                --count;
                int next = inputStream.readInt();
                result |= (next == part);
            }
            return result;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception during getting file parts info from seeder");
        }
        return false;
    }

    public synchronized void downloadFile(FileEntry entry) {
        if (P2PServer.myFiles.get(entry.id) != null) {
            return;
        }
        P2PServer.myFiles.put(entry.id, new FileStatus(entry.id, entry.size, entry.name, false, Paths.get(Torrent.DOWNLOAD_DIRECTORY, entry.name)));
        executorService.submit(new DownloadTask(entry));
    }

    private class UpdateTask extends TimerTask {
        private boolean needToRecall = true;
        public UpdateTask() {
            super();
        }
        public UpdateTask(boolean needToRecall) {
            super();
            this.needToRecall = needToRecall;
        }
        @Override
        public void run() {
            lock.lock();
            try {
                logger.log(Level.FINE, "Starting sending update request!");
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                List<FileStatus> files = new LinkedList<>(P2PServer.myFiles.values());
                outputStream.writeByte(4);
                outputStream.writeInt(p2pServer.getPort());
                outputStream.writeInt(files.size());
                for (FileStatus file : files) {
                    outputStream.writeInt(file.id);
                }
                outputStream.flush();
                boolean result = inputStream.readBoolean();
                if (!result) {
                    logger.log(Level.WARNING, "Got \"failed to update\" response!");
                }
                logger.log(Level.FINE, "Sending update request : done");
                if (needToRecall) {
                    timer.schedule(new UpdateTask(), Torrent.UPDATE_DELAY);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Filed to send update! Exception : " + e.toString());
            }
            lock.unlock();
        }
    }

    public static class FileEntry {
        public final int id;
        public final String name;
        public final long size;

        public FileEntry(int id, String name, long size) {
            this.id = id;
            this.name = name;
            this.size = size;
        }
    }

    public class DownloadTask implements Runnable {
        private final FileEntry fileEntry;

        public DownloadTask(FileEntry fileEntry) {
            this.fileEntry = fileEntry;
        }

        @Override
        public void run() {
            try {
                createDownloadDirectory();
                List<SeederDescription> seeders = null;
                while (seeders == null || seeders.size() == 0) {
                    seeders = getSeeders(fileEntry.id);
                }
                logger.log(Level.FINE, "Got seeders!");
                Path filePath = Paths.get(Torrent.DOWNLOAD_DIRECTORY, fileEntry.name);
                RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
                file.setLength(fileEntry.size);
                FileStatus fileStatus = P2PServer.myFiles.get(fileEntry.id);

                while (true) {
                    BitSet partStatus = fileStatus.lock();
                    if (partStatus.nextClearBit(0) < 0 || partStatus.nextClearBit(0) >= fileStatus.blocks) {
                        fileStatus.unlock();
                        break;
                    }
                    fileStatus.unlock();
                    int nextPos = partStatus.nextClearBit(0);
                    logger.log(Level.FINE, "Processing part #" + nextPos);
                    for (SeederDescription seeder : seeders) {
                        if (checkSeederContainsPart(seeder, fileEntry.id, nextPos)) {
                            logger.log(Level.FINE, "Got nice seeder..." + nextPos);
                            byte[] buffer = new byte[fileStatus.calculatePartSize(nextPos)];
                            try (Socket socket = new Socket(InetAddress.getByAddress(seeder.ip), seeder.getPort())) {
                                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                                outputStream.writeByte(2);
                                outputStream.writeInt(fileEntry.id);
                                outputStream.writeInt(nextPos);
                                outputStream.flush();
                                inputStream.readFully(buffer);
                                file.seek(nextPos * Torrent.PART_SIZE);
                                file.write(buffer, 0, buffer.length);
                                partStatus = fileStatus.lock();
                                partStatus.set(nextPos);
                                fileStatus.unlock();
                            } catch (IOException exc) {
                                logger.log(Level.WARNING, "Failed to download next part : " + exc.toString());
                            }
                        }
                    }
                }
                file.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Something really bad happened : " + e.toString());
            }
            logger.log(Level.FINE, "Finished downloading!");
        }

        private void createDownloadDirectory() throws IOException {
            final Path downloadDirectory = Paths.get(Torrent.DOWNLOAD_DIRECTORY);
            if (!downloadDirectory.toFile().exists()) {
                Files.createDirectories(downloadDirectory);
            }
        }
    }
}
