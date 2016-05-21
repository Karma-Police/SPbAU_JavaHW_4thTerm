package ru.spbau.mit;

import org.junit.Test;
import ru.spbau.mit.P2P.P2PHandler;
import ru.spbau.mit.P2P.P2PServer;

/**
 * Created by michael on 15.05.16.
 */

public class ServerTest {
    @Test
    public void runAndStop() {
        Server server = new Server(5001);
        server.run(ClientHandler::new);
        server.stop();
    }

    @Test
    public void runP2PAndStop() {
        P2PServer p2pServer = new P2PServer(5002);
        p2pServer.run(P2PHandler::new);
        p2pServer.stop();
    }
}
