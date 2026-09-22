package dev.pinaki.backstage.library.impl.http.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class MapUtilTest {
    private final Map<String, String> values = Collections.singletonMap("key", "value");

    @Test
    public void readsRequiredAndOptionalValues() {
        assertEquals("value", MapUtil.required(values, "key"));
        assertEquals("", MapUtil.valueOrEmpty(values, "missing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingRequiredValue() {
        MapUtil.required(values, "missing");
    }
}
