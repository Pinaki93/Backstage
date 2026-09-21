package dev.pinaki.backstage.library.impl.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import dev.pinaki.backstage.library.impl.http.util.MapUtil;

public final class RequestBody {
    private final byte[] bytes;

    public RequestBody(byte[] bytes) {
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
    }

    public byte[] asBytes() {
        return bytes.clone();
    }

    public String asString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Map<String, String> asMap() {
        return MapUtil.fromQuery(asString());
    }
}
