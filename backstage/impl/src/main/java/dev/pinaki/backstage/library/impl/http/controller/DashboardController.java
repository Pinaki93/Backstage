package dev.pinaki.backstage.library.impl.http.controller;

import java.io.IOException;

import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.di.BackstageContainer;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.Middleware;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class DashboardController implements Middleware {

    private final ControllerFactory controllerFactory;

    public DashboardController(BackstageContainer container) {
        controllerFactory = container.controllerFactory();
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return "/backstage".equals(request.getPath()) ||
                ("/".equals(request.getPath()) &&
                        !controllerFactory.keyValueControllers().isEmpty());
    }

    @Override
    public boolean handle(HttpRequest request) throws IOException {
        StringBuilder links = new StringBuilder();
        for (BasicController controller : controllerFactory.basicControllers()) {
            if (!"/".equals(controller.getPath())) appendLink(links, controller.getPath(),
                    controller.getPath());
        }
        for (KeyValueController controller :
                controllerFactory.keyValueControllers().values()) {
            appendLink(links, controller.getPath(), controller.getTitle());
        }

        if (links.length() == 0) links.append("<li>No controllers registered</li>");

        String body = "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Backstage</title><style>" + styles() + "</style></head><body>"
                + "<main><h1>Backstage</h1><p>Registered controllers</p><ul>" + links
                + "</ul></main></body></html>";
        return ResponseUtil.html(request, body);
    }

    private static void appendLink(StringBuilder output, String path, String label) {
        output.append("<li><a href=\"").append(encode(path)).append("\">")
                .append(encode(label)).append(" <small>").append(encode(path))
                .append("</small></a></li>");
    }

    private static String encode(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String styles() {
        return "body{font:16px system-ui;background:#f5f5f7;color:#202124;margin:0}"
                + "main{max-width:760px;margin:48px auto;padding:24px}h1{margin-bottom:4px}"
                + "ul{padding:0;list-style:none}li{background:white;border:1px solid #ddd;"
                + "border-radius:10px;margin:12px 0}a{display:block;padding:18px;color:#1557b0;"
                + "text-decoration:none}small{color:#666;float:right}";
    }
}
