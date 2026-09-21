package dev.pinaki.backstage.library.impl.kv;

import java.io.IOException;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;

public interface KeyValueRequestHandler {
    boolean handle(HttpRequest request, KeyValueController controller) throws IOException;
}
