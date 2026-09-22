package dev.pinaki.backstage.library;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class KeyValueControllerTest {
    @Test
    public void exposesNormalizedMetadataAndEntries() {
        KeyValueController controller = controller("Preferences", "/preferences",
                callback -> callback.onChanged(Collections.singletonMap("theme", "dark")));

        assertEquals("Preferences", controller.getTitle());
        assertEquals("/preferences", controller.getPath());
        assertEquals("dark", controller.getEntries().get("theme"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRootPath() {
        controller("Preferences", "/", callback -> callback.onChanged(Collections.emptyMap()));
    }

    @Test
    public void callbackUpdatesObserversAndCanBeUnsubscribed() {
        ReactiveDataSource source = new ReactiveDataSource();
        KeyValueController controller = controller("Preferences", "/preferences", source);
        AtomicReference<Map<String, String>> observedEntries = new AtomicReference<>();
        KeyValueController.Subscription subscription = controller.observe(observedEntries::set);

        source.notifySubscribers(Collections.singletonMap("theme", "light"));
        assertEquals("light", observedEntries.get().get("theme"));

        subscription.close();

        source.notifySubscribers(Collections.singletonMap("theme", "dark"));
        assertEquals("light", observedEntries.get().get("theme"));
    }

    private KeyValueController controller(String title, String path,
                                          KeyValueController.EntriesSource source) {
        return new KeyValueController(title, path, source) {
            @Override
            public void create(String key, String value) {
            }

            @Override
            public void update(String key, String value) {
            }

            @Override
            public void delete(String key) {
            }

            @Override
            public void clear() {
            }
        };
    }

    private static final class ReactiveDataSource implements KeyValueController.EntriesSource,
            KeyValueController.EntriesCallback {
        private final List<KeyValueController.EntriesCallback> subscribers = new ArrayList<>();

        @Override
        public void subscribe(KeyValueController.EntriesCallback callback) {
            subscribers.add(callback);
        }

        public void notifySubscribers(Map<String, String> entries) {
            for (KeyValueController.EntriesCallback subscriber : subscribers) {
                subscriber.onChanged(entries);
            }
        }

        @Override
        public void onChanged(Map<String, String> entries) {
            notifySubscribers(entries);
        }
    }
}
