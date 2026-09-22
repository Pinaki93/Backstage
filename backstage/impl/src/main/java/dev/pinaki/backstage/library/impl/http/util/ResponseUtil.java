package dev.pinaki.backstage.library.impl.http.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import dev.pinaki.backstage.library.impl.http.HttpRequest;

/** Writes the response types supported by the Backstage HTTP server. */
public final class ResponseUtil {
    private ResponseUtil() {
    }

    public static boolean text(HttpRequest request, int code, String reason, String body)
            throws IOException {
        return respond(request, code, reason, "text/plain; charset=utf-8",
                StreamUtil.bytes(body), "");
    }

    public static boolean html(HttpRequest request, String body) throws IOException {
        return respond(request, 200, "OK", "text/html; charset=utf-8",
                StreamUtil.bytes(body), "");
    }

    public static boolean html(HttpRequest request, byte[] body) throws IOException {
        return respond(request, 200, "OK", "text/html; charset=utf-8", body, "");
    }

    public static boolean json(HttpRequest request, Map<String, String> values) throws IOException {
        return respond(request, 200, "OK", "application/json; charset=utf-8",
                StreamUtil.bytes(toJson(values)), "");
    }

    public static boolean noContent(HttpRequest request) throws IOException {
        return respond(request, 204, "No Content", "text/plain; charset=utf-8", new byte[0], "");
    }

    public static boolean methodNotAllowed(HttpRequest request, String allow) throws IOException {
        return respond(request, 405, "Method Not Allowed", "text/plain; charset=utf-8",
                StreamUtil.bytes("Method Not Allowed"), "Allow: " + allow + "\r\n");
    }

    public static boolean bytes(HttpRequest request, String contentType, byte[] body)
            throws IOException {
        return respond(request, 200, "OK", contentType, body, "");
    }

    public static OutputStream eventStream(HttpRequest request) throws IOException {
        return request.startEventStream();
    }

    public static String toJson(Map<String, String> values) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (json.length() > 1) json.append(',');
            json.append('"').append(jsonString(entry.getKey())).append("\":\"")
                    .append(jsonString(entry.getValue())).append('"');
        }
        return json.append('}').toString();
    }

    private static boolean respond(HttpRequest request, int code, String reason, String contentType,
                                   byte[] body, String headers) throws IOException {
        request.respond(code, reason, contentType, body, request.isHead(), headers);
        return true;
    }

    private static String jsonString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }
}
