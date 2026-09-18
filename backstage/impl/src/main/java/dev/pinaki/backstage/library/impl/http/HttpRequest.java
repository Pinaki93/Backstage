package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;

import dev.pinaki.backstage.library.impl.http.util.HttpUtil;
import dev.pinaki.backstage.library.impl.http.util.RequestReader;
import dev.pinaki.backstage.library.impl.util.Lazy;

public class HttpRequest {

    public static final String GET = "GET";
    public static final String HEAD = "HEAD";

    private final Socket client;
    private final String method;
    private final String path;
    private final String httpVersion;
    private final Lazy<String> assetPath;
    private final LinkedHashMap<String, String> headers;

    public HttpRequest(Socket client, String method, String requestTarget, String httpVersion,
                        LinkedHashMap<String, String> headers) {
        this.client = client;
        this.method = method;
        this.path = requestTarget;
        this.httpVersion = httpVersion;
        this.headers = headers;
        assetPath = Lazy.wrap(() -> HttpUtil.assetPath(path));
    }

    public String getMethod() {
        return method;
    }

    public String assetPath() {
        return assetPath.getWithoutSync();
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public boolean isHead() {
        return HttpRequest.HEAD.equals(getMethod());
    }

    public boolean isValid() {
        return getMethod() != null && getHttpVersion() != null && path != null;
    }

    public void respond(int code, String reason, String body)
            throws IOException {
        HttpResponse.with(code, reason, body).writeToClient(client);
    }

    public void respond(int code, String reason, String contentType,
                        byte[] body, boolean head, String extraHeaders) throws IOException {
        HttpResponse.with(code, reason, contentType, body, head, extraHeaders)
                .writeToClient(client);
    }
}
