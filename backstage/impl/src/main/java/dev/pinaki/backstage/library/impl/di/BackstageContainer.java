package dev.pinaki.backstage.library.impl.di;

import dev.pinaki.backstage.library.impl.http.controller.ControllerFactory;

public final class BackstageContainer {
    private final ControllerFactory controllerFactory = new ControllerFactory();

    public ControllerFactory controllerFactory() {
        return controllerFactory;
    }
}
