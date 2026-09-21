package dev.pinaki.backstage.library.impl.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class RequestBodyTest {
    @Test
    public void convertsBody() {
        byte[] bytes = "key=hello+world&empty=".getBytes(StandardCharsets.UTF_8);
        RequestBody body = new RequestBody(bytes);

        assertArrayEquals(bytes, body.asBytes());
        assertEquals("key=hello+world&empty=", body.asString());
        assertEquals("hello world", body.asMap().get("key"));
        assertEquals("", body.asMap().get("empty"));
    }

    @Test
    public void treatsNullAsEmpty() {
        RequestBody body = new RequestBody(null);

        assertEquals("", body.asString());
        assertEquals(0, body.asMap().size());
    }
}
