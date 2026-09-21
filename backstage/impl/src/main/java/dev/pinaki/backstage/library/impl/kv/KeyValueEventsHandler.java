package dev.pinaki.backstage.library.impl.kv;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class KeyValueEventsHandler implements KeyValueRequestHandler {
    @Override
    public boolean handle(HttpRequest request, KeyValueController controller) throws IOException {
        if (!HttpRequest.GET.equals(request.getMethod())) {
            return ResponseUtil.methodNotAllowed(request, "GET");
        }
        Log.d("shared_pref", "Opening event stream: " + controller.getPath());
        OutputStream output = ResponseUtil.eventStream(request);
        BlockingQueue<Map<String, String>> updates = new ArrayBlockingQueue<>(1);
        try (KeyValueController.Subscription ignored = controller.observe(entries -> {
            updates.clear();
            updates.offer(entries);
        })) {
            while (!Thread.currentThread().isInterrupted()) {
                Map<String, String> entries;
                try {
                    entries = updates.take();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
                try {
                    output.write(("data: " + ResponseUtil.toJson(entries) + "\n\n")
                            .getBytes(StandardCharsets.UTF_8));
                    output.flush();
                } catch (IOException disconnected) {
                    Log.d("shared_pref", "Event stream disconnected: " + controller.getPath());
                    break;
                }
            }
        }
        Log.d("shared_pref", "Closed event stream: " + controller.getPath());

        return true;
    }
}
