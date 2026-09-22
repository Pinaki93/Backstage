package dev.pinaki.backstage.library.impl.http.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapUtil {
    private MapUtil() {
    }

    public static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    public static String valueOrEmpty(Map<String, String> values, String key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }

    public static Map<String, String> fromQuery(String encoded) {
        if (encoded.isEmpty()) return Collections.emptyMap();
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : encoded.split("&")) {
            String[] halves = part.split("=", 2);
            values.put(decode(halves[0]), decode(halves.length == 1 ? "" : halves[1]));
        }
        return values;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
