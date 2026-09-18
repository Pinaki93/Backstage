package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;

public interface Middleware {
    boolean handle(HttpRequest request, RequestChain chain) throws IOException;
}
