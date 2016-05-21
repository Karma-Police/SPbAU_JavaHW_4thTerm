package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class ClientHandler extends SocketHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final SeederDescription seeder;
    private final Torrent torrent;

    private TimerTask timerTask;

    public ClientHandler(@NotNull Socket socket) {
        super(socket);
        seeder = new SeederDescription(socket.getInetAddress().getAddress());
        torrent = Torrent.getInstance();
        timerTask = new SeederKiller();
    }

    @Override
    protected void handleRequest(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException, SeederDescription.SeederCreatingException {
        RequestType requestType = RequestType.getServerRequest(inputStream.readByte());
        switch (requestType) {
            case UPDATE:
                logger.log(Level.FINE, "Got update request from : " + socketInfo);
                processUpdate(inputStream, outputStream);
                break;
            case UPLOAD:
                logger.log(Level.FINE, "Got upload request from : " + socketInfo);
                processUpload(inputStream, outputStream);
                break;
            case GET_FILES:
                logger.log(Level.INFO, "Got get-files request from : " + socketInfo);
                processGetFiles(outputStream);
                break;
            case GET_FILE_SEEDERS:
                logger.log(Level.INFO, "Got get-file-seeders request from : " + socketInfo);
                processGetSeeders(inputStream, outputStream);
                break;
            default:
                logger.log(Level.WARNING, "Unknown request type from : " + socketInfo);
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
            ok &= torrent.addSeederToFile(seeder, id);
        }
        timerTask = new SeederKiller();
        torrent.timer.schedule(timerTask, Torrent.UPDATE_DELAY);
        outputStream.writeBoolean(ok);
        outputStream.flush();
    }

    private void processUpload(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        String name = inputStream.readUTF();
        long size = inputStream.readLong();
        int result = torrent.submitNewFile(name, size);
        outputStream.writeInt(result);
        outputStream.flush();
    }

    private void processGetFiles(DataOutputStream outputStream) throws IOException {
        List<FileDescription> files = torrent.getFiles();
        outputStream.writeInt(files.size());
        for (FileDescription description : files) {
            outputStream.writeInt(description.id);
            outputStream.writeUTF(description.name);
            outputStream.writeLong(description.size);
        }
        outputStream.flush();
    }

    private void processGetSeeders(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int fileID = inputStream.readInt();
        List<SeederDescription> seeders = torrent.getSeedersOf(fileID);
        if (seeders == null) {
            outputStream.writeInt(0);
            outputStream.flush();
            return;
        }
        outputStream.writeInt(seeders.size());
        for (SeederDescription next : seeders) {
            outputStream.write(next.ip);
            outputStream.writeShort(next.getPort());
        }
        outputStream.flush();
    }

    private class SeederKiller extends TimerTask {
        @Override
        public void run() {
            seeder.kill();
        }
    }
}
