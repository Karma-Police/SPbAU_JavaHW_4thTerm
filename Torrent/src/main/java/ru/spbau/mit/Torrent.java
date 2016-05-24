package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class Torrent {
    public static final long DEATH_DELAY = 6 * 60 * 1000;
    public static final long UPDATE_DELAY = 5 * 60 * 1000;
    public static final int PART_SIZE = 10 * 1024 * 1024;
    public static final String HOST_NAME = "localhost";
    public static final short SERVER_PORT = 8081;
    public static final String DOWNLOAD_DIRECTORY = "downloads";

    private static final Logger logger = Logger.getLogger(Torrent.class.getName());
    private static Torrent instance = null;

    public final Timer timer;

    private final ConcurrentHashMap<Integer, FileDescription> files;

    public static synchronized Torrent getInstance() {
        if (instance == null) {
            instance = new Torrent();
        }
        return instance;
    }

    public boolean addSeederToFile(SeederDescription seeder, int fileID) {
        FileDescription description = files.get(fileID);
        if (description == null) {
            logger.log(Level.WARNING, "Failed to add seeder to file with id=" + fileID + " cause it is not found!");
            return false;
        }
        description.seeders.add(seeder);
        return true;
    }

    public int submitNewFile(@NotNull String name, long size) {
        if (size < 0) {
            logger.log(Level.WARNING, "Failed to submit new file cause file size is negative.");
            return -1;
        }
        FileDescription file = new FileDescription(size, name);
        files.put(file.id, file);
        return file.id;
    }

    public List<FileDescription> getFiles() {
        return new LinkedList<>(files.values());
    }

    public List<SeederDescription> getSeedersOf(int fileID) {
        FileDescription file = files.get(fileID);
        if (file == null) {
            logger.log(Level.WARNING, "Failed to get seeders cause the file with fileID = " + fileID + " not found!");
            return null;
        }
        file.seeders.removeIf(seeder -> !seeder.isAlive());
        return new LinkedList<>(file.seeders);
    }

    private Torrent() {
        files = new ConcurrentHashMap<>();
        timer = new Timer();
    }
}
