package dev.pinaki.backstage.library.impl.http.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.KeyValueController;

public class ControllerFactory {
    private final List<BasicController> controllers = new CopyOnWriteArrayList<>();
    private final Map<String, KeyValueController> keyValueControllers =
            new ConcurrentHashMap<>();

    public ControllerFactory() { }

    public void addController(BasicController controller) {
        controllers.add(controller);
    }

    public void addKeyValueController(KeyValueController controller) {
        keyValueControllers.put(controller.getPath(), controller);
    }

    public List<BasicController> basicControllers() {
        return controllers;
    }

    public Map<String, KeyValueController> keyValueControllers() {
        return keyValueControllers;
    }

    public KeyValueController getKeyValueController(String path) {
        return keyValueControllers.get(path);
    }
}
