package dev.pinaki.backstage.library.impl.http.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StreamUtilTest {
    @Test
    public void readLineSupportsCrLfLfAndEndOfStream() throws IOException {
        ByteArrayInputStream input = input("first\r\nsecond\nthird");

        assertEquals("first", StreamUtil.readLine(input));
        assertEquals("second", StreamUtil.readLine(input));
        assertEquals("third", StreamUtil.readLine(input));
        assertNull(StreamUtil.readLine(input));
    }

    @Test(expected = IOException.class)
    public void readLineRejectsOversizedLines() throws IOException {
        StreamUtil.readLine(input(new String(new char[8 * 1024 + 1]).replace('\0', 'a')));
    }

    @Test
    public void readFullyReadsAllBytes() throws IOException {
        byte[] value = "contents".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(value, StreamUtil.readFully(new ByteArrayInputStream(value)));
    }

    @Test
    public void bytesUsesUtf8() {
        assertArrayEquals(new byte[]{(byte) 0xc3, (byte) 0xa9}, StreamUtil.bytes("é"));
    }

    private static ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII));
    }
}
