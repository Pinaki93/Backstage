package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.RequestChain;
import dev.pinaki.backstage.library.impl.http.util.StreamUtil;

/** Serves registered controllers and falls through when none owns the request path. */
public final class ControllerMiddleware implements Middleware {
    private final List<BasicController> controllers;
    private final ResourceSource resources;

    public ControllerMiddleware(List<BasicController> controllers, ResourceSource resources) {
        this.controllers = controllers;
        this.resources = resources;
    }

    @Override
    public boolean handle(HttpRequest request, RequestChain chain) throws IOException {
        for (BasicController controller : controllers) {
            if (controller.getPath().equals(request.getPath())) {
                try (InputStream html = resources.open(controller.getHtmlResource())) {
                    request.respond(200, "OK", "text/html; charset=utf-8",
                            StreamUtil.readFully(html), request.isHead(), "");
                }
                return true;
            }
        }
        return chain.handle();
    }

    public interface ResourceSource {
        InputStream open(int resourceId) throws IOException;
    }
}
