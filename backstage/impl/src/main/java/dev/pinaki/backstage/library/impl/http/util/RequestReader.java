package dev.pinaki.backstage.library.impl.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

public class RequestReader {

    public static RequestReader from(InputStream inputStream) {
        return new RequestReader(inputStream);
    }

    private final InputStream inputStream;

    private RequestReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String[] readRequestLine() throws IOException {
        String line = StreamUtil.readLine(inputStream);
        if (line == null) return null;

        String[] parts = line.split(" ", 3);
        if (parts.length != 3) return null;

        return parts;
    }

    public LinkedHashMap<String, String> readHeaders()
            throws IOException {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();

        String line;

        while ((line = StreamUtil.readLine(inputStream)) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');

            if (colonIndex == -1) {
                // malformed header
                continue;
            }

            String name = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();

            headers.put(name, value);
        }

        return headers;
    }

    public byte[] readBody(int length) throws IOException {
        if (length < 0 || length > 1_048_576) throw new IOException("Invalid Content-Length");
        byte[] body = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = inputStream.read(body, offset, length - offset);
            if (count < 0) throw new IOException("Unexpected end of request body");
            offset += count;
        }
        return body;
    }
}
