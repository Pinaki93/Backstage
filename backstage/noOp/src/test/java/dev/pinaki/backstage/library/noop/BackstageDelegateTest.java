package dev.pinaki.backstage.library.noop;

import org.junit.Test;

import dev.pinaki.backstage.library.BackstageDelegate;
import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.KeyValueController;
import java.util.Collections;

public class BackstageDelegateTest {
    @Test
    public void supportsInitializationWithoutPages() {
        BackstageDelegate delegate = new BackstageDelegate(null);

        delegate.init();
        delegate.addController(new BasicController("/test") {
            @Override
            public int getHtmlResource() {
                return 1;
            }
        });
        delegate.addController(new KeyValueController("Test", "/values",
                callback -> callback.onChanged(Collections.emptyMap())) {
            @Override public void create(String key, String value) { }
            @Override public void update(String key, String value) { }
            @Override public void delete(String key) { }
            @Override public void clear() { }
        });
        delegate.addSharedPreferencesExplorer("Preferences", () -> {
            throw new AssertionError("no-op must not create SharedPreferences");
        });
    }
}
