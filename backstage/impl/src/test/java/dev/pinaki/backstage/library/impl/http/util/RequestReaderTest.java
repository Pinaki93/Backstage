package dev.pinaki.backstage.library.impl.http.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class RequestReaderTest {
    @Test
    public void readsRequestLineAndHeaders() throws IOException {
        RequestReader reader = reader("GET /index.html HTTP/1.1\r\nHost: localhost\r\nX-Test: a:b\r\n\r\n");

        assertArrayEquals(new String[]{"GET", "/index.html", "HTTP/1.1"},
                reader.readRequestLine());
        LinkedHashMap<String, String> headers = reader.readHeaders();
        assertEquals("localhost", headers.get("Host"));
        assertEquals("a:b", headers.get("X-Test"));
    }

    @Test
    public void malformedRequestLineReturnsNull() throws IOException {
        assertNull(reader("GET /\r\n").readRequestLine());
        assertNull(reader("").readRequestLine());
    }

    @Test
    public void malformedHeaderIsIgnored() throws IOException {
        RequestReader reader = reader("GET / HTTP/1.1\r\nMalformed\r\nGood: value\r\n\r\n");
        reader.readRequestLine();

        LinkedHashMap<String, String> headers = reader.readHeaders();
        assertFalse(headers.containsKey("Malformed"));
        assertEquals("value", headers.get("Good"));
    }

    private static RequestReader reader(String input) {
        return RequestReader.from(new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII)));
    }
}
