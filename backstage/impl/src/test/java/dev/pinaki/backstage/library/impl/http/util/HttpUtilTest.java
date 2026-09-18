package dev.pinaki.backstage.library.impl.http.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class HttpUtilTest {
    @Test
    public void mapsRootAndDirectoriesToIndex() {
        assertEquals("index.html", HttpUtil.assetPath("/"));
        assertEquals("docs/index.html", HttpUtil.assetPath("/docs/"));
    }

    @Test
    public void normalizesRepeatedSlashesAndDecodesPath() {
        assertEquals("assets/app.js", HttpUtil.assetPath("//assets//app%2Ejs?version=2"));
    }

    @Test
    public void rejectsUnsafeOrInvalidPaths() {
        assertNull(HttpUtil.assetPath("relative/file"));
        assertNull(HttpUtil.assetPath("/../secret"));
        assertNull(HttpUtil.assetPath("/%2e/secret"));
        assertNull(HttpUtil.assetPath("/folder\\secret"));
        assertNull(HttpUtil.assetPath("/bad%zz"));
        assertNull(HttpUtil.assetPath("/bad%00path"));
    }
}
