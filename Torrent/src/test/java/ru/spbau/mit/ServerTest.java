package ru.spbau.mit;

import org.junit.Test;

/**
 * Created by michael on 15.05.16.
 */

public class ServerTest {
    @Test
    public void runAndStop() {
        Server server = new Server(5001);
        server.run();
        server.stop();
    }

}
