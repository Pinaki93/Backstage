package dev.pinaki.backstage.library;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class BackstageTest {
    @Test
    public void getInstanceReturnsSingleton() {
        String[] explorerName = new String[1];
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

            @Override
            public void addSharedPreferencesExplorer(String displayName,
                                                     Backstage.SharedPreferencesFactory factory) {
                explorerName[0] = displayName;
            }
        };
        Backstage instance = Backstage.getInstance(delegate);

        assertNotNull(instance);
        assertSame(instance, Backstage.getInstance(delegate));
        assertSame(instance, instance.withSharedPrefExplorer("App Settings", () -> null));
        assertEquals("App Settings", explorerName[0]);
        assertSame(instance, instance.withLoggingLevel(BackstageLog.Level.INFO));
        assertEquals(BackstageLog.Level.INFO, BackstageLog.getLevel());
        BackstageLog.setLevel(BackstageLog.Level.DEBUG);
    }
}
