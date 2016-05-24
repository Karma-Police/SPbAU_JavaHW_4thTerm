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
    public final int blocks;
    private final BitSet partStatus;
    private final Lock lock;

    public FileStatus(int id, long size, String name, boolean isDownloaded, Path path) {
        this.id = id;
        this.size = size;
        this.name = name;
        this.path = path;
        blocks = (int) ((size + Torrent.PART_SIZE - 1) / Torrent.PART_SIZE);
        partStatus = new BitSet(blocks);
        lock = new ReentrantLock();
        if (isDownloaded) {
            partStatus.set(0, blocks);
        }
        logger.log(Level.FINE, "File name: " + name + ", size = " + size + ", split into " + blocks + "pieces.");
    }

    public BitSet lock() {
        lock.lock();
        return partStatus;
    }

    public void unlock() {
        lock.unlock();
    }

    public int calculatePartSize(int id) {
        if (id < blocks - 1) {
            return Torrent.PART_SIZE;
        } else {
            return (int) (size - Torrent.PART_SIZE * (blocks - 1));
        }
    }

    public int calculateProgress() {
        lock.lock();
        int res = 0;
        for (int i = 0; i < blocks; i++) {
            res += partStatus.get(i) ? 1 : 0;
        }
        lock.unlock();
        return (int) (100 * res / (double) blocks);
    }

    public boolean isDownloaded() {
        lock.lock();
        boolean res = partStatus.nextClearBit(0) < 0 || partStatus.nextClearBit(0) >= blocks;
        lock.unlock();
        return res;
    }
}
