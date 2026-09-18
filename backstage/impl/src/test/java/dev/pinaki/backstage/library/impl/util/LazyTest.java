package dev.pinaki.backstage.library.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class LazyTest {
    @Test
    public void getComputesValueOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        Object value = new Object();
        Lazy<Object> lazy = Lazy.wrap(() -> {
            calls.incrementAndGet();
            return value;
        });

        assertSame(value, lazy.get());
        assertSame(value, lazy.get());
        assertEquals(1, calls.get());
    }

    @Test
    public void getWithoutSyncComputesValueOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<String> lazy = Lazy.wrap(() -> "value-" + calls.incrementAndGet());

        assertEquals("value-1", lazy.getWithoutSync());
        assertEquals("value-1", lazy.getWithoutSync());
        assertEquals(1, calls.get());
    }
}
