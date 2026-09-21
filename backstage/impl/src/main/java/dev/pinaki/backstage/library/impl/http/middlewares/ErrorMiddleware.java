package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;

import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public class ErrorMiddleware implements Middleware {
    @Override
    public boolean canHandle(HttpRequest request) {
        return !request.isValid()
                || (!request.isHead() && !HttpRequest.GET.equals(request.getMethod()))
                || request.assetPath() == null;
    }

    @Override
    public boolean handle(HttpRequest request) throws IOException {
        if (!request.isValid()) {
            return ResponseUtil.text(request, 400, "Bad Request", "Malformed request");
        }
        if (!request.isHead() && !HttpRequest.GET.equals(request.getMethod())) {
            return ResponseUtil.methodNotAllowed(request, "GET, HEAD");
        }
        if (request.assetPath() == null) {
            return ResponseUtil.text(request, 400, "Bad Request", "Invalid path");
        }
        return false;
    }
}
