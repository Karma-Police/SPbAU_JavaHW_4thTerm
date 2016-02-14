package mit.spbau.ru;

import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by michael on 12.02.16.
 */

public class LazyFactoryTest {
    private static class IncrementSupplier implements Supplier<Integer> {
        private int counter = 0;
        private boolean executionAllowed = false;

        public void allowExecuting() {
            executionAllowed = true;
        }

        @Override
        public Integer get() {
            assertTrue(executionAllowed);
            counter += 1;
            return counter;
        }
    }

    private static class NullSupplier implements Supplier<Integer> {
        @Override
        public Integer get() {
            return null;
        }
    }

    private void multiThreadTest(Lazy<Integer> lazy) {
        ArrayList<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(() -> {
                    assertEquals((Integer) 1, lazy.get());
                    assertEquals((Integer) 1, lazy.get());
                    assertEquals((Integer) 1, lazy.get());
                }));
        }
        for (int i = 0; i < 100; i++) {
            threadList.get(i).start();
        }
        for (int i = 0; i < 100; i++) {
            try {
                threadList.get(i).join();
            } catch (InterruptedException exc) {
                exc.printStackTrace();
                throw new AssertionError();
            }
        }
    }

    @Test
    public void createLazyTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazy(incrementSupplier);
        incrementSupplier.allowExecuting();
        assertEquals((Integer) 1, lazy.get());
        assertEquals((Integer) 1, lazy.get());
    }

    @Test
    public void createLazyConcurrentTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test
    public void createLazyConcurrentAtomicTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrentAtomic(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test
    public void nullTest() {
        NullSupplier nullSupplier = new NullSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazy(nullSupplier);
        Lazy<Integer> lazyC = LazyFactory.createLazyConcurrent(nullSupplier);
        Lazy<Integer> lazyCA = LazyFactory.createLazyConcurrentAtomic(nullSupplier);
        assertEquals(null, lazy.get());
        assertEquals(null, lazyC.get());
        assertEquals(null, lazyCA.get());
        assertEquals(null, lazy.get());
        assertEquals(null, lazyC.get());
        assertEquals(null, lazyCA.get());
    }
}


