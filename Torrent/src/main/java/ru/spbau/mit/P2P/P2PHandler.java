package ru.spbau.mit.P2P;

import ru.spbau.mit.RequestType;
import ru.spbau.mit.SeederDescription;
import ru.spbau.mit.SocketHandler;
import ru.spbau.mit.Torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 16.05.16.
 */

public class P2PHandler extends SocketHandler {
    private static final Logger logger = Logger.getLogger(P2PHandler.class.getName());

    public P2PHandler(Socket socket) {
        super(socket);
    }

    @Override
    protected void handleRequest(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException, SeederDescription.SeederCreatingException {
        RequestType requestType = RequestType.getP2PRequest(inputStream.readByte());
        switch (requestType) {
            case STAT:
                logger.log(Level.FINE, "Got stat request from : " + socketInfo);
                processStat(inputStream, outputStream);
                break;
            case GET_FILE_PART:
                logger.log(Level.FINE, "Got get-file-part request from : " + socketInfo);
                processDownload(inputStream, outputStream);
                break;
            default:
                logger.log(Level.WARNING, "Unknown request type from : " + socketInfo);
        }
    }


    private void processStat(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int fileID = inputStream.readInt();
        FileStatus fileStatus = P2PServer.myFiles.get(fileID);
        if (fileStatus == null) {
            logger.log(Level.INFO, "Requested file not found. FileID = " + fileID);
            outputStream.writeInt(0);
            return;
        }
        List<Integer> readyPieces = new LinkedList<>();
        BitSet info = fileStatus.lock();
        for (int i = info.nextSetBit(0); i >= 0; i = info.nextSetBit(i + 1)) {
            readyPieces.add(i);
        }
        fileStatus.unlock();
        outputStream.writeInt(readyPieces.size());
        for (int i : readyPieces) {
            outputStream.writeInt(i);
        }
        outputStream.flush();
    }

    private void processDownload(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int fileID = inputStream.readInt();
        int part = inputStream.readInt();
        FileStatus fileStatus = P2PServer.myFiles.get(fileID);
        if (fileStatus == null) {
            logger.log(Level.INFO, "Requested file not found. FileID = " + fileID);
            return;
        }
        BitSet status = fileStatus.lock();
        if (fileStatus.blocks <= part || !status.get(part)) {
            logger.log(Level.INFO, "Peer doesn't have required part (" + part + ")");
            fileStatus.unlock();
            return;
        }
        fileStatus.unlock();
        DataInputStream fileStream = new DataInputStream(Files.newInputStream(fileStatus.path));
        fileStream.skipBytes(part * Torrent.PART_SIZE);
        byte[] buffer = new byte[fileStatus.calculatePartSize(part)];
        fileStream.readFully(buffer);
        fileStream.close();
        outputStream.write(buffer);
        outputStream.flush();
    }

}
