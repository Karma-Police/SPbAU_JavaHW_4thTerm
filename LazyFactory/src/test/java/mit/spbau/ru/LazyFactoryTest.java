package mit.spbau.ru;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
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
        private boolean firstTime = true;
        @Override
        public Integer get() {
            assertTrue(firstTime);
            firstTime = false;
            return null;
        }
    }

    private void multiThreadTest(Lazy<Integer> lazy) throws InterruptedException {
        ArrayList<Thread> threadList = new ArrayList<>();
        CyclicBarrier barrier = new CyclicBarrier(100);
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(() -> {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException exc) {
                        fail();
                    }
                    assertEquals((Integer) 1, lazy.get());
                    assertEquals((Integer) 1, lazy.get());
                    assertEquals((Integer) 1, lazy.get());
                }));
        }
        for (int i = 0; i < 100; i++) {
            threadList.get(i).start();
        }
        for (int i = 0; i < 100; i++) {
            threadList.get(i).join();
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
    public void createLazyConcurrentTest() throws InterruptedException {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test
    public void createLazyConcurrentAtomicTest() throws InterruptedException {
        IncrementSupplier incrementSupplier = new IncrementSupplier();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrentAtomic(incrementSupplier);
        incrementSupplier.allowExecuting();
        multiThreadTest(lazy);
    }

    @Test
    public void nullTest() {
        Lazy<Integer> lazy = LazyFactory.createLazy(new NullSupplier());
        Lazy<Integer> lazyC = LazyFactory.createLazyConcurrent(new NullSupplier());
        Lazy<Integer> lazyCA = LazyFactory.createLazyConcurrentAtomic(new NullSupplier());
        assertEquals(null, lazy.get());
        assertEquals(null, lazyC.get());
        assertEquals(null, lazyCA.get());
        assertEquals(null, lazy.get());
        assertEquals(null, lazyC.get());
        assertEquals(null, lazyCA.get());
    }
}
