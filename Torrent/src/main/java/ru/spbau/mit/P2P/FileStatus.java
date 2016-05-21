package ru.spbau.mit.P2P;

import ru.spbau.mit.Torrent;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 16.05.16.
 */

public class FileStatus {
    private static final Logger logger = Logger.getLogger(FileStatus.class.getName());
    public final int id;
    public final long size;
    public final String name;
    public final Path path;
    private final BitSet partStatus;
    private final Lock lock;

    public FileStatus(int id, long size, String name, boolean isDownloaded, Path path) {
        this.id = id;
        this.size = size;
        this.name = name;
        this.path = path;
        partStatus = new BitSet((int) ((size + Torrent.PART_SIZE - 1) / Torrent.PART_SIZE));
        lock = new ReentrantLock();
        if (isDownloaded) {
            partStatus.set(0, partStatus.size(), true);
        }
        logger.log(Level.FINE, "File name: " + name + ", size = " + size + ", split into " + partStatus.size() + "pieces.");
    }

    public BitSet lock() {
        lock.lock();
        return partStatus;
    }

    public void unlock() {
        lock.unlock();
    }

    public int calculatePartSize(int id) {
        if (id < partStatus.size() - 1) {
            return Torrent.PART_SIZE;
        } else {
            return (int) (size - Torrent.PART_SIZE * (partStatus.size() - 1));
        }
    }
}
