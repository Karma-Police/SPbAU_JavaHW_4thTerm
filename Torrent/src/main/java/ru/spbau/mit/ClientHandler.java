package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final SeederDescription seeder;
    private final Torrent torrent;
    private final String clientInfo;

    private TimerTask timerTask;

    public ClientHandler(@NotNull Socket socket) {
        this.socket = socket;
        seeder = new SeederDescription(socket.getInetAddress().getAddress());
        torrent = Torrent.getInstance();
        timerTask = new SeederKiller();
        clientInfo = socket.getInetAddress().toString();
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
            if (!socket.isClosed()) {
                logger.log(Level.WARNING, "Got exception : " + e.toString() + ";  from " + clientInfo);
            }
        }
    }

    private void handleRequest(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        RequestType requestType = RequestType.getServerRequest(inputStream.readByte());
        switch (requestType) {
            case UPDATE:
                logger.log(Level.FINE, "Got update request from : " + clientInfo);
                processUpdate(inputStream, outputStream);
                break;
            case UPLOAD:
                logger.log(Level.FINE, "Got upload request from : " + clientInfo);
                processUpload(inputStream, outputStream);
                break;
            case GET_FILES:
                logger.log(Level.INFO, "Got get-files request from : " + clientInfo);
                processGetFiles(outputStream);
                break;
            case GET_FILE_SEEDERS:
                logger.log(Level.INFO, "Got get-file-seeders request from : " + clientInfo);
                processGetSeeders(inputStream, outputStream);
                break;
            default:
                logger.log(Level.WARNING, "Unknown request type from : " + clientInfo);
        }
    }

    private void processUpdate(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        timerTask.cancel();
        short port = inputStream.readShort();
        seeder.setPort(port);
        seeder.resurrect();
        int count = inputStream.readInt();
        boolean ok = true;
        for (int i = 0; i < count; i++) {
            int id = inputStream.readInt();
            ok &= torrent.addSeedToFile(seeder, id);
        }
        timerTask = new SeederKiller();
        torrent.timer.schedule(timerTask, Torrent.UPDATE_DELAY);
        outputStream.writeBoolean(ok);
    }

    private void processUpload(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        String name = inputStream.readUTF();
        long size = inputStream.readLong();
        int result = torrent.submitNewFile(name, size);
        outputStream.writeInt(result);
    }

    private void processGetFiles(DataOutputStream outputStream) throws IOException {
        List<FileDescription> files = torrent.getFiles();
        outputStream.writeInt(files.size());
        for (FileDescription description : files) {
            outputStream.writeInt(description.id);
            outputStream.writeUTF(description.name);
            outputStream.writeLong(description.size);
        }
    }

    private void processGetSeeders(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int fileID = inputStream.readInt();
        List<SeederDescription> seeders = torrent.getSeedersOf(fileID);
        outputStream.writeInt(seeders.size());
        for (SeederDescription next : seeders) {
            outputStream.write(next.ip);
            outputStream.writeShort(next.getPort());
        }
    }

    private class SeederKiller extends TimerTask {
        @Override
        public void run() {
            seeder.kill();
        }
    }
}
