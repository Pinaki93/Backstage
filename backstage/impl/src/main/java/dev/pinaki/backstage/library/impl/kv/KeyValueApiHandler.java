package dev.pinaki.backstage.library.impl.kv;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.util.MapUtil;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class KeyValueApiHandler implements KeyValueRequestHandler {
    @Override
    public boolean handle(HttpRequest request, KeyValueController controller) throws IOException {
        try {
            if (controller.getPath().startsWith("/shared-preferences/")) {
                Log.d("shared_pref", "Handling API method: " + request.getMethod());
            }
            if (HttpRequest.GET.equals(request.getMethod())) {
                return ResponseUtil.json(request, controller.getEntries());
            } else if (HttpRequest.POST.equals(request.getMethod())) {
                Map<String, String> form = request.getBody().asMap();
                controller.create(MapUtil.required(form, "key"),
                        MapUtil.valueOrEmpty(form, "value"));
            } else if (HttpRequest.PUT.equals(request.getMethod())) {
                Map<String, String> form = request.getBody().asMap();
                controller.update(MapUtil.required(form, "key"),
                        MapUtil.valueOrEmpty(form, "value"));
            } else if (HttpRequest.DELETE.equals(request.getMethod())) {
                String key = MapUtil.fromQuery(request.getQuery()).get("key");
                if (key == null) {
                    controller.clear();
                } else {
                    controller.delete(key);
                }
            } else {
                return ResponseUtil.methodNotAllowed(request, "GET, POST, PUT, DELETE");
            }
        } catch (IllegalArgumentException invalid) {
            if (controller.getPath().startsWith("/shared-preferences/")) {
                Log.e("shared_pref", "SharedPreferences request failed", invalid);
            }
            return ResponseUtil.text(request, 400, "Bad Request",
                    invalid.getMessage() == null ? "Bad Request" : invalid.getMessage());
        }
        return ResponseUtil.noContent(request);
    }
}
