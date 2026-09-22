package dev.pinaki.backstage.library;

import static org.junit.Assert.assertEquals;

import android.content.SharedPreferences;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedPreferencesKeyValueControllerTest {
    @Test
    public void readsWritesAndReactivelyPublishesPreferences() {
        FakePreferences fake = new FakePreferences();
        fake.values.put("launches", 3);
        AtomicInteger factoryCalls = new AtomicInteger();
        SharedPreferencesKeyValueController controller =
                new SharedPreferencesKeyValueController("App Settings", () -> {
                    factoryCalls.incrementAndGet();
                    return fake.preferences();
                });
        AtomicReference<Map<String, String>> observed = new AtomicReference<>();

        assertEquals(0, factoryCalls.get());
        controller.observe(observed::set);

        assertEquals(1, factoryCalls.get());
        assertEquals("/shared-preferences/app-settings", controller.getPath());
        assertEquals("3", observed.get().get("launches"));

        controller.create("theme", "dark");
        assertEquals("dark", observed.get().get("theme"));
        controller.update("theme", "light");
        assertEquals("light", fake.values.get("theme"));
        controller.delete("theme");
        assertEquals(false, observed.get().containsKey("theme"));
        controller.clear();
        assertEquals(true, observed.get().isEmpty());
    }

    private static final class FakePreferences {
        private final Map<String, Object> values = new LinkedHashMap<>();
        private SharedPreferences.OnSharedPreferenceChangeListener listener;

        private SharedPreferences preferences() {
            return (SharedPreferences) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{SharedPreferences.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getAll": return new LinkedHashMap<>(values);
                            case "registerOnSharedPreferenceChangeListener":
                                listener = (SharedPreferences.OnSharedPreferenceChangeListener) args[0];
                                return null;
                            case "edit": return editor((SharedPreferences) proxy);
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }

        private SharedPreferences.Editor editor(SharedPreferences preferences) {
            AtomicReference<String> changedKey = new AtomicReference<>();
            return (SharedPreferences.Editor) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{SharedPreferences.Editor.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "putString": values.put((String) args[0], args[1]); changedKey.set((String) args[0]); return proxy;
                            case "remove": values.remove(args[0]); changedKey.set((String) args[0]); return proxy;
                            case "clear": values.clear(); changedKey.set(null); return proxy;
                            case "apply": listener.onSharedPreferenceChanged(preferences, changedKey.get()); return null;
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
