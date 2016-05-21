package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 15.05.16.
 */

public class SeederDescription {
    private static final Logger logger = Logger.getLogger(SeederDescription.class.getName());
    private static long ID = 0;

    public final long id;
    public final byte[] ip;
    private volatile short port;
    private volatile boolean isAlive;

    public SeederDescription(@NotNull byte[] ip) {
        if (ip.length != 4) {
            String msg = "Invalid seeder's ip: " + ip;
            logger.log(Level.SEVERE, msg);
            throw new SeederCreatingException(msg);
        } else {
            this.id = ID++;
            this.ip = ip;
            isAlive = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().isAssignableFrom(SeederDescription.class)) {
            return id == ((SeederDescription) o).id;
        } else {
            return false;
        }
    }

    public void setPort(short newPort) {
        port = newPort;
    }

    public short getPort() {
        return port;
    }

    public void kill() {
        isAlive = false;
    }

    public void resurrect() {
        isAlive = true;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public static class SeederCreatingException extends IllegalArgumentException {
        public SeederCreatingException(String msg) {
            super(msg);
        }
    }
}
