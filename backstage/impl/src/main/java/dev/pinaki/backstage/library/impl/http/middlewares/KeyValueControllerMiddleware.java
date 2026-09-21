package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.di.BackstageContainer;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.controller.ControllerFactory;
import dev.pinaki.backstage.library.impl.kv.KeyValueApiHandler;
import dev.pinaki.backstage.library.impl.kv.KeyValueEventsHandler;
import dev.pinaki.backstage.library.impl.kv.KeyValuePageHandler;
import dev.pinaki.backstage.library.impl.kv.KeyValueRequestHandler;

/**
 * Resolves key-value routes and dispatches them to commands.
 */
public final class KeyValueControllerMiddleware implements Middleware {
    private final ControllerFactory controllerFactory;
    private final Map<String, KeyValueRequestHandler> handlers = new LinkedHashMap<>();

    public KeyValueControllerMiddleware(BackstageContainer container) {
        this.controllerFactory = container.controllerFactory();
        handlers.put("", new KeyValuePageHandler());
        handlers.put("/api", new KeyValueApiHandler());
        handlers.put("/events", new KeyValueEventsHandler());
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        /*
         * Ideally we can do a sanity check as done in handle method.
         * But it makes no sense to compute that twice. We can return true from here.
         * In case we can't handle this request, handle will return false and the chain can proceed.
         * */
        return request.getPath() != null;
    }

    @Override
    public boolean handle(HttpRequest request) throws IOException {
        String path = request.getPath();

        for (Map.Entry<String, KeyValueRequestHandler> entry : handlers.entrySet()) {
            String suffix = entry.getKey();
            if (!path.endsWith(suffix)) continue;
            String controllerPath = path.substring(0, path.length() - suffix.length());
            KeyValueController controller = controllerFactory.getKeyValueController(controllerPath);
            if (controller != null) {
                KeyValueRequestHandler handler = entry.getValue();
                return handler.handle(request, controller);
            }
        }

        return false;
    }
}
