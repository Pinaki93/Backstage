package dev.pinaki.backstage.library.impl.http.middlewares;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import dev.pinaki.backstage.library.impl.http.BackstageHttpServer;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.RequestChain;
import dev.pinaki.backstage.library.impl.http.util.StreamUtil;

public class AssetMiddleware implements Middleware {

    private final BackstageHttpServer.AssetSource assets;

    public AssetMiddleware(BackstageHttpServer.AssetSource assets) {
        this.assets = assets;
    }

    @Override
    public boolean handle(HttpRequest request, RequestChain chain) throws IOException {
        try (InputStream asset = assets.open(request.assetPath())) {
            request.respond(200, "OK", contentType(request.assetPath()),
                    StreamUtil.readFully(asset), request.isHead(), "");
            return true;
        } catch (IOException missing) {
            return false;
        }
    }

    private static String contentType(String path) {
        String lower = path.toLowerCase(Locale.US);
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js") || lower.endsWith(".mjs"))
            return "text/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }
}
