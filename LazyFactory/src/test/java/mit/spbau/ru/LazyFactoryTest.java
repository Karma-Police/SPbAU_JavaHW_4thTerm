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

    private void multiThreadTest(Lazy<Integer> lazy) {
        ArrayList<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(() -> assertEquals(lazy.get(), (Integer) 1)));
        }
        for (int i = 0; i < 100; i++) {
            threadList.get(i).run();
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
        assertEquals(lazy.get(), (Integer) 1);
        assertEquals(lazy.get(), (Integer) 1);
        assertEquals(lazy.get(), (Integer) 1);
    }

    @Test(expected = AssertionError.class)
    public void createLazyFailTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazy(incrementSupplier);
        lazy.get();
    }

    @Test
    public void createLazyConcurrentTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test(expected = AssertionError.class)
    public void createLazyConcurrentTestFail() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(incrementSupplier);
        lazy.get();
    }

    @Test
    public void createLazyConcurrentAtomicTest() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrentAtomic(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test(expected = AssertionError.class)
    public void createLazyConcurrentAtomicTestFail() {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrentAtomic(incrementSupplier);
        lazy.get();
    }
}


