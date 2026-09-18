package dev.pinaki.backstage.library;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class BackstageTest {
    @Test
    public void getInstanceReturnsSingleton() {
        Backstage instance = Backstage.getInstance();

        assertNotNull(instance);
        assertSame(instance, Backstage.getInstance());
    }
}
