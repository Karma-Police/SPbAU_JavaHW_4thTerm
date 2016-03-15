package mit.spbau.ru;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by michael on 14.03.16.
 */

public class SimpleFtpTest {
    private static final String HOSTNAME = "127.0.0.1";
    private static final String ROOT = "./src/test/resources/";
    private static final int PORT = 12124;
    private static final String cf218dir = "Atask.cpp true, Btask true, Ctask true, Dtask true, Etask true, ";
    private static final String bTask = "include <iostream> include <cstdio>";

    private boolean multiThreadTestOk = true;

    @Test
    public void simpleInterfaceTest() {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);
        server.start();
        client.connect();
        List<String> list = client.executeList(ROOT + "cf218/");
        list.sort(Comparator.<String>naturalOrder());
        String result = "";
        for (String s : list) {
            result += s + ", ";
        }
        assertEquals(cf218dir, result);
        assertEquals(bTask, client.executeGet(ROOT + "cf218/Btask/Btask.cpp").toString());
        client.disconnect();
        server.stop();
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        FtpServer server = new FtpServer(PORT);
        server.start();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new TypicalClient()));
            threads.get(i).start();
        }
        for (int i = 0; i < 100; i++) {
            threads.get(i).join();
        }
        server.stop();
        assertTrue(multiThreadTestOk);
    }

    private class TypicalClient implements Runnable {
        @Override
        public void run() {
            FtpClient client = new FtpClient(HOSTNAME, PORT);
            client.connect();
            List<String> list = client.executeList(ROOT + "cf218/");
            list.sort(Comparator.<String>naturalOrder());
            String result = "";
            for (String s : list) {
                result += s + ", ";
            }
            try {
                assertEquals(cf218dir, result);
                assertEquals(bTask, client.executeGet(ROOT + "cf218/Btask/Btask.cpp").toString());
            } catch (AssertionError e) {
                e.printStackTrace();
                multiThreadTestOk = false;
            }
            client.disconnect();
        }
    }
}
