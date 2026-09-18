package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import dev.pinaki.backstage.library.impl.http.util.StreamUtil;

public class HttpResponse {

    private final int code;
    private final String reason;
    private final String contentType;
    private final byte[] body;
    private final boolean head;
    private final String extraHeaders;

    public static HttpResponse with(int code, String reason, String body) {
        return new HttpResponse(code, reason, "text/plain; charset=utf-8",
                StreamUtil.bytes(body), false, "");
    }

    public static HttpResponse with(int code, String reason, String contentType, byte[] body,
                                    boolean head, String extraHeaders) {
        return new HttpResponse(code, reason, contentType, body, head, extraHeaders);
    }


    private HttpResponse(int code, String reason, String contentType, byte[] body, boolean head,
                         String extraHeaders) {
        this.code = code;
        this.reason = reason;
        this.contentType = contentType;
        this.body = body;
        this.head = head;
        this.extraHeaders = extraHeaders;
    }

    public void writeToClient(Socket client) throws IOException {
        String headers = "HTTP/1.1 " + code + " " + reason + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n" + extraHeaders
                + "Connection: close\r\n\r\n";
        OutputStream output = client.getOutputStream();
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        if (!head) output.write(body);
        output.flush();
    }
}
