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
    public static final long UPDATE_DELAY = 60000;

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

    public boolean addSeedToFile(SeederDescription seeder, int fileID) {
        FileDescription description = files.get(fileID);
        if (description == null) {
            logger.log(Level.WARNING, "File with id " + fileID + " not found!");
            return false;
        }
        description.seeders.add(seeder);
        return true;
    }

    public int submitNewFile(@NotNull String name, long size) {
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
            logger.log(Level.WARNING, "No file with fileID = " + fileID);
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
