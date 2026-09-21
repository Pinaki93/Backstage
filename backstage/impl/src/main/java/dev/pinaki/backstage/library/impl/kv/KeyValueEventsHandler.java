package dev.pinaki.backstage.library.impl.kv;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class KeyValueEventsHandler implements KeyValueRequestHandler {
    @Override
    public boolean handle(HttpRequest request, KeyValueController controller) throws IOException {
        if (!HttpRequest.GET.equals(request.getMethod())) {
            return ResponseUtil.methodNotAllowed(request, "GET");
        }
        OutputStream output = ResponseUtil.eventStream(request);
        Object monitor = new Object();
        AtomicBoolean connected = new AtomicBoolean(true);
        try (KeyValueController.Subscription ignored = controller.observe(entries -> {
            try {
                output.write(("data: " + ResponseUtil.toJson(entries) + "\n\n")
                        .getBytes(StandardCharsets.UTF_8));
                output.flush();
            } catch (IOException disconnected) {
                connected.set(false);
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        })) {
            synchronized (monitor) {
                while (connected.get()) monitor.wait();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
