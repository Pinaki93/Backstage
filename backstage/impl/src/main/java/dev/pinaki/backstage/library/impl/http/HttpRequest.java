package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;
import java.net.Socket;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import dev.pinaki.backstage.library.impl.http.util.HttpUtil;
import dev.pinaki.backstage.library.util.Lazy;

public class HttpRequest {

    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    private final Socket client;
    private final String method;
    private final String path;
    private final String httpVersion;
    private final Lazy<String> assetPath;
    private final LinkedHashMap<String, String> headers;
    private final RequestBody body;

    public HttpRequest(Socket client, String method, String requestTarget, String httpVersion,
                       LinkedHashMap<String, String> headers, byte[] body) {
        this.client = client;
        this.method = method;
        this.path = requestTarget;
        this.httpVersion = httpVersion;
        this.headers = headers;
        assetPath = Lazy.wrap(() -> HttpUtil.assetPath(path));
        this.body = new RequestBody(body == null ? new byte[0] : body.clone());
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        if (path == null) return null;
        int query = path.indexOf('?');
        return query >= 0 ? path.substring(0, query) : path;
    }

    public String getQuery() {
        if (path == null) return null;
        int query = path.indexOf('?');
        return query >= 0 ? path.substring(query + 1) : "";
    }

    public String getHeader(String name) {
        if (headers == null) return null;
        for (java.util.Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(name)) return header.getValue();
        }
        return null;
    }

    public RequestBody getBody() {
        return body;
    }

    public OutputStream startEventStream() throws IOException {
        OutputStream output = client.getOutputStream();
        output.write(("HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/event-stream; charset=utf-8\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: keep-alive\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        output.flush();
        return output;
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
