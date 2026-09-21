package dev.pinaki.backstage.library;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class BackstageTest {
    @Test
    public void getInstanceReturnsSingleton() {
        Backstage.Delegate delegate = new Backstage.Delegate() {
            @Override
            public void init() {
            }

            @Override
            public void addController(BasicController controller) {
            }

            @Override
            public void addController(KeyValueController controller) {
            }
        };
        Backstage instance = Backstage.getInstance(delegate);

        assertNotNull(instance);
        assertSame(instance, Backstage.getInstance(delegate));
    }
}
