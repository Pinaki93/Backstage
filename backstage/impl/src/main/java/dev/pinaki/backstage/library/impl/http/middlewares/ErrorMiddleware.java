package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;

import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.RequestChain;
import dev.pinaki.backstage.library.impl.http.util.StreamUtil;

public class ErrorMiddleware implements Middleware {
    @Override
    public boolean handle(HttpRequest request, RequestChain chain) throws IOException {
        if (!request.isValid()) {
            request.respond(400, "Bad Request", "Malformed request");
            return true;
        }
        if (!request.isHead() && !HttpRequest.GET.equals(request.getMethod())) {
            request.respond(405, "Method Not Allowed",
                    "text/plain; charset=utf-8",
                    StreamUtil.bytes("Method Not Allowed"), false,
                    "Allow: GET, HEAD\r\n");
            return true;
        }
        if (request.assetPath() == null) {
            request.respond(400, "Bad Request", "Invalid path");
            return true;
        }
        if (!chain.handle()) {
            request.respond(404, "Not Found", "Not Found");
        }
        return true;
    }
}
