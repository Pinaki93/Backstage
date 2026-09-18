package dev.pinaki.backstage.library.impl.http.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    private HttpUtil() {

    }

    public static String assetPath(String target) {
        int query = target.indexOf('?');
        String raw = query >= 0 ? target.substring(0, query) : target;
        try {
            String decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
            if (!decoded.startsWith("/") || decoded.indexOf('\0') >= 0) return null;
            StringBuilder clean = new StringBuilder();
            for (String segment : decoded.substring(1).split("/", -1)) {
                if (segment.isEmpty()) continue;
                if (".".equals(segment) || "..".equals(segment) || segment.indexOf('\\') >= 0) {
                    return null;
                }
                if (clean.length() > 0) clean.append('/');
                clean.append(segment);
            }
            if (clean.length() == 0 || decoded.endsWith("/")) {
                if (clean.length() > 0) clean.append('/');
                clean.append("index.html");
            }
            return clean.toString();
        } catch (IllegalArgumentException | IOException invalidPath) {
            return null;
        }
    }
}
