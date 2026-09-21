package dev.pinaki.backstage.library.impl.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestChainTest {
    @Test
    public void skipsCommandsThatCannotHandleRequest() throws IOException {
        AtomicBoolean skippedCommandRan = new AtomicBoolean();
        AtomicBoolean matchingCommandRan = new AtomicBoolean();
        Middleware skipped = command(false, skippedCommandRan);
        Middleware matching = command(true, matchingCommandRan);
        HttpRequest request = new HttpRequest(new Socket(), "GET", "/", "HTTP/1.1",
                new LinkedHashMap<>(), null);

        boolean handled = new RequestChain(request)
                .withMiddlewares(Arrays.asList(skipped, matching))
                .handle();

        assertTrue(handled);
        assertFalse(skippedCommandRan.get());
        assertTrue(matchingCommandRan.get());
    }

    private static Middleware command(boolean canHandle, AtomicBoolean ran) {
        return new Middleware() {
            @Override
            public boolean canHandle(HttpRequest request) {
                return canHandle;
            }

            @Override
            public boolean handle(HttpRequest request) {
                ran.set(true);
                return true;
            }
        };
    }
}
