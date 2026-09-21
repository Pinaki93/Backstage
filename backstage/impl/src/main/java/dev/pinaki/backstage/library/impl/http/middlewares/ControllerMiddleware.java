package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;
import java.io.InputStream;

import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.impl.di.BackstageContainer;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.controller.ControllerFactory;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;
import dev.pinaki.backstage.library.impl.http.util.StreamUtil;

/**
 * Serves registered controllers and falls through when none owns the request path.
 */
public final class ControllerMiddleware implements Middleware {
    private final ResourceSource resources;
    private final ControllerFactory controllerFactory;


    public ControllerMiddleware(BackstageContainer container, ResourceSource resources) {
        this.controllerFactory = container.controllerFactory();
        this.resources = resources;
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        for (BasicController controller : controllerFactory.basicControllers()) {
            if (controller.getPath().equals(request.getPath())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean handle(HttpRequest request) throws IOException {
        for (BasicController controller : controllerFactory.basicControllers()) {
            if (controller.getPath().equals(request.getPath())) {
                try (InputStream html = resources.open(controller.getHtmlResource())) {
                    ResponseUtil.html(request, StreamUtil.readFully(html));
                }
                return true;
            }
        }
        return false;
    }

    public interface ResourceSource {
        InputStream open(int resourceId) throws IOException;
    }
}
