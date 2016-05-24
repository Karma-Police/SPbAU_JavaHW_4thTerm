package ru.spbau.mit.P2P;

import ru.spbau.mit.Server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by michael on 17.05.16.
 */

public class P2PServer extends Server {
    public static final ConcurrentHashMap<Integer, FileStatus> myFiles = new ConcurrentHashMap<>();

    public P2PServer(int port) {
        super(port);
    }

    @Override
    public void stop() {
        super.stop();
        myFiles.clear();
    }
}
