package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class FileDescription {
    private static final Logger logger = Logger.getLogger(FileDescription.class.getName());
    private static int ID = 0;

    public final int id;
    public final long size;
    public final String name;
    public final Set<SeederDescription> seeders;

    public FileDescription(long size, @NotNull String name) {
        if (size <= 0) {
            String msg = "Invalid file size : " + size;
            logger.log(Level.WARNING, msg);
            throw new IllegalArgumentException(msg);
        }
        this.id = ID++;
        this.size = size;
        this.name = name;
        this.seeders = new ConcurrentSkipListSet<>(Comparator.comparingLong(val -> val.id));
    }
}
