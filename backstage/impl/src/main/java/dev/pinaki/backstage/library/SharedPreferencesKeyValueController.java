package dev.pinaki.backstage.library;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

final class SharedPreferencesKeyValueController extends KeyValueController {
    private static final String TAG = "shared_pref";
    private final PreferencesSource source;

    SharedPreferencesKeyValueController(String displayName,
                                        Backstage.SharedPreferencesFactory factory) {
        this(displayName, new PreferencesSource(factory));
        Log.d(TAG, "Created explorer: " + getPath());
    }

    private SharedPreferencesKeyValueController(String displayName, PreferencesSource source) {
        super(displayName, path(displayName), source);
        this.source = source;
        Log.d(TAG, "Configured explorer: " + getPath());
    }

    @Override protected void onAccess() {
        Log.d(TAG, "Accessing explorer: " + getPath());
        source.start();
    }

    @Override public void create(String key, String value) {
        Log.d(TAG, "Creating key: " + key);
        source.preferences().edit().putString(key, value).apply();
    }

    @Override public void update(String key, String value) {
        Log.d(TAG, "Updating key: " + key);
        source.preferences().edit().putString(key, value).apply();
    }

    @Override public void delete(String key) {
        Log.d(TAG, "Deleting key: " + key);
        source.preferences().edit().remove(key).apply();
    }

    @Override public void clear() {
        Log.d(TAG, "Clearing preferences");
        source.preferences().edit().clear().apply();
    }

    private static String path(String displayName) {
        Log.d(TAG, "Creating path for: " + displayName);
        if (displayName == null) throw new IllegalArgumentException("displayName must not be null");
        String slug = displayName.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) throw new IllegalArgumentException("displayName must contain a letter or number");
        return "/shared-preferences/" + slug;
    }

    private static Map<String, String> entries(SharedPreferences preferences) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            entries.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        Log.d(TAG, "Read " + entries.size() + " entries");
        return entries;
    }

    private static final class PreferencesSource implements EntriesSource {
        private final Backstage.SharedPreferencesFactory factory;
        private SharedPreferences preferences;
        private SharedPreferences.OnSharedPreferenceChangeListener listener;
        private EntriesCallback callback;

        private PreferencesSource(Backstage.SharedPreferencesFactory factory) {
            this.factory = factory;
            Log.d(TAG, "Created lazy preferences source");
        }

        @Override public void subscribe(EntriesCallback callback) {
            Log.d(TAG, "Subscribed controller callback");
            this.callback = callback;
        }

        private synchronized SharedPreferences preferences() {
            Log.d(TAG, "Requesting SharedPreferences");
            start();
            return preferences;
        }

        private synchronized void start() {
            if (preferences != null) {
                Log.d(TAG, "SharedPreferences already initialized");
                return;
            }
            Log.d(TAG, "Initializing SharedPreferences");
            preferences = factory.create();
            if (preferences == null) {
                throw new IllegalArgumentException("factory must return SharedPreferences");
            }
            listener = (ignored, key) -> {
                Log.d(TAG, "Preference changed: " + key);
                callback.onChanged(entries(preferences));
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);
            Log.d(TAG, "Registered preference change listener");
            callback.onChanged(entries(preferences));
        }
    }
}
