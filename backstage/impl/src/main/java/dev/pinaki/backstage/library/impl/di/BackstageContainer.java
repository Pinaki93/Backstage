package dev.pinaki.backstage.library.impl.di;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.pinaki.backstage.library.impl.http.controller.ControllerFactory;
import dev.pinaki.backstage.library.util.Lazy;

public final class BackstageContainer {

    public static BackstageContainer INSTANCE = null;

    public static BackstageContainer getInstance() {
        if (INSTANCE == null) {
            synchronized (BackstageContainer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BackstageContainer();
                }
            }
        }

        return INSTANCE;
    }

    public static void teardown() {
        INSTANCE = null;
    }

    private final ControllerFactory controllerFactory = new ControllerFactory();
    private final Lazy<ExecutorService> cachedExecutorFactory = Lazy.wrap(() -> {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "Backstage HTTP client");
            thread.setDaemon(true);
            return thread;
        });
    });

    public ControllerFactory controllerFactory() {
        return controllerFactory;
    }

    public ExecutorService cachedExecutor() {
        return cachedExecutorFactory.get();
    }
}
