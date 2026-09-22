package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;

public interface Middleware {
    boolean canHandle(HttpRequest request);

    boolean handle(HttpRequest request) throws IOException;
}
