package dev.pinaki.backstage.library;

import android.content.Context;

import java.io.IOException;

import dev.pinaki.backstage.library.impl.http.BackstageHttpServer;

public final class BackstageDelegate implements Backstage.Delegate {
    private final BackstageHttpServer server;

    public BackstageDelegate(Context context) {
        server = new BackstageHttpServer(context);
    }

    @Override
    public void init() {
        try {
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to start Backstage", exception);
        }
    }

    @Override
    public void addController(BasicController controller) {
        server.addController(controller);
    }

    @Override
    public void addController(KeyValueController controller) {
        server.addController(controller);
    }

    @Override
    public void addSharedPreferencesExplorer(String displayName,
                                             Backstage.SharedPreferencesFactory factory) {
        BackstageLog.d("shared_pref", "Registering explorer: " + displayName);
        server.addController(new SharedPreferencesKeyValueController(displayName,
                factory));
    }
}
