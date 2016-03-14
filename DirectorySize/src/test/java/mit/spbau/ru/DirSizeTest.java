package mit.spbau.ru;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by michael on 01.03.16.
 */

public class DirSizeTest {
    @Test
    public void simpleTest() {
        assertEquals(DirSize.getDirectorySize(new File("./src/test/resources/justAFolder")),
                DirSize.getDirectorySizeSlow(new File("./src/test/resources/justAFolder")));

        System.err.println(DirSize.getDirectorySize(new File("./src/test/resources/justAFolder")));
        assertEquals(DirSize.getDirectorySize(new File("./src/test/resources/justAFolder")), 1424);
    }

    @Test
    public void timeTest() {
        long slow_time = System.currentTimeMillis();
        long tmp1 = DirSize.getDirectorySizeSlow(new File("/home/michael/Documents"));
        slow_time = System.currentTimeMillis() - slow_time;

        long fast_time = System.currentTimeMillis();
        long tmp2 = DirSize.getDirectorySize(new File("/home/michael/Documents"));
        fast_time = System.currentTimeMillis() - fast_time;

        System.err.println(tmp1);
        System.err.println(tmp2);
        System.err.println("Fast time: " + Long.toString(fast_time) + ",  slow time: " + Long.toString(slow_time));
    }
}
