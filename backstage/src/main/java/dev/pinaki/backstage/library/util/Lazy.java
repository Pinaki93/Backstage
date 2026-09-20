package dev.pinaki.backstage.library.util;

public class Lazy<T> {
    private volatile T value = null;
    private final Producer<T> producer;

    public static <T> Lazy<T> wrap(Producer<T> producer) {
        return new Lazy<>(producer);
    }

    private Lazy(Producer<T> producer) {
        this.producer = producer;
    }

    public T get() {
        if (value == null) {
            synchronized (producer) {
                if (value == null) {
                    value = producer.produce();
                }
            }
        }

        return value;
    }

    public T getWithParam() {
        if (value == null) {
            synchronized (producer) {
                if (value == null) {
                    value = producer.produce();
                }
            }
        }

        return value;
    }

    public T getWithoutSync() {
        if (value == null) {
            value = producer.produce();
        }

        return value;
    }

    public interface Producer<T> {
        T produce();
    }
}
