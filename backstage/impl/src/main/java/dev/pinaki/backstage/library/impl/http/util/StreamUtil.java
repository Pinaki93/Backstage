package dev.pinaki.backstage.library.impl.http.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamUtil {
    private static final int MAX_REQUEST_LINE_BYTES = 8 * 1024;

    public static String readLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int current;
        while ((current = inputStream.read()) != -1 && current != '\n') {
            if (current != '\r') {
                if (line.size() >= MAX_REQUEST_LINE_BYTES)
                    throw new IOException("Request too long");
                line.write(current);
            }
        }
        return current == -1 && line.size() == 0 ? null
                : line.toString(StandardCharsets.US_ASCII.name());
    }

    public static byte[] readFully(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8 * 1024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    public static void sendText(OutputStream output, int code, String reason, String body)
            throws IOException {
        send(output, code, reason, "text/plain; charset=utf-8", bytes(body), false, "");
    }

    public static void send(OutputStream output, int code, String reason, String contentType,
                             byte[] body, boolean head, String extraHeaders) throws IOException {
        String headers = "HTTP/1.1 " + code + " " + reason + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n" + extraHeaders
                + "Connection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        if (!head) output.write(body);
        output.flush();
    }

    public static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
