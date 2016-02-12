package mit.spbau.ru;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by michael on 12.02.16.
 */

public class LazyFactory {
    private final static Object DEFAULD_UTIL_OBJ = new Object();

    public static <T> Lazy<T> createLazy(Supplier<T> supplier) {
        return new Lazy<T>() {
            private Object result = LazyFactory.DEFAULD_UTIL_OBJ;

            @Override
            public T get() {
                if (result == LazyFactory.DEFAULD_UTIL_OBJ) {
                    result = supplier.get();
                }
                return (T) result;
            }
        };
    }

    public static <T> Lazy<T> createLazyConcurrent(Supplier<T> supplier) {
        return new Lazy<T>() {
            private volatile Object result = LazyFactory.DEFAULD_UTIL_OBJ;

            @Override
            public T get() {
                if (result == LazyFactory.DEFAULD_UTIL_OBJ) {
                    synchronized (this) {
                        if (result == LazyFactory.DEFAULD_UTIL_OBJ) {
                            result = supplier.get();
                        }
                    }
                }
                return (T) result;
            }
        };
    }

    public static <T> Lazy<T> createLazyConcurrentAtomic(Supplier<T> supplier) {
        return new AtomicLazy<>(supplier);
    }

    private static class AtomicLazy<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private volatile Object result = LazyFactory.DEFAULD_UTIL_OBJ;

        public AtomicLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            AtomicReferenceFieldUpdater<AtomicLazy, Object> updator =
                    AtomicReferenceFieldUpdater.newUpdater(AtomicLazy.class, Object.class, "result");
            updator.compareAndSet(this, LazyFactory.DEFAULD_UTIL_OBJ, supplier.get());
            return (T) result;
        }
    }
}

