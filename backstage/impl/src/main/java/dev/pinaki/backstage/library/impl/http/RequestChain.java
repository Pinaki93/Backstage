package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import dev.pinaki.backstage.library.impl.http.util.RequestReader;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class RequestChain {
    private final List<Middleware> middlewares = new ArrayList<>();
    private final HttpRequest httpRequest;

    public static class Executor {
        private final List<Middleware> middlewares;

        public static Executor getInstance() {
            return new Executor(new ArrayList<>());
        }

        public Executor addMiddleware(Middleware middleware) {
            middlewares.add(middleware);
            return this;
        }

        private Executor(List<Middleware> middlewares) {
            this.middlewares = middlewares;
        }

        public void execute(Socket client) {
            try (Socket socket = client;
                 OutputStream ignored = socket.getOutputStream()) {
                RequestChain chain = RequestChain
                        .withClient(socket)
                        .withMiddlewares(middlewares);
                if (!chain.handle()) {
                    ResponseUtil.text(chain.httpRequest, 404, "Not Found", "Not Found");
                }
            } catch (IOException ignored) {
                // A client may disconnect at any point.
            }
        }
    }

    private static RequestChain withClient(Socket client) throws IOException {
        client.setSoTimeout(5_000);
        RequestReader reader = RequestReader.from(client.getInputStream());
        String[] parts = reader.readRequestLine();
        HttpRequest httpRequest;
        if (parts == null) {
            // Consume the rest of the request before closing the connection so the client can
            // reliably receive the error response instead of seeing a TCP reset.
            reader.readHeaders();
            httpRequest = new HttpRequest(client, null, null, null,
                    null, null);
        } else {
            java.util.LinkedHashMap<String, String> headers = reader.readHeaders();
            int contentLength = 0;
            for (java.util.Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey().equalsIgnoreCase("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(header.getValue());
                    } catch (NumberFormatException invalid) {
                        contentLength = -1;
                    }
                }
            }
            httpRequest = new HttpRequest(client, parts[0], parts[1], parts[2], headers,
                    reader.readBody(contentLength));
        }

        return new RequestChain(httpRequest);
    }

    RequestChain(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public RequestChain withMiddlewares(List<Middleware> middleware) {
        middlewares.addAll(middleware);
        return this;
    }

    public boolean handle() throws IOException {
        for (Middleware middleware : middlewares) {
            if (middleware.canHandle(httpRequest) &&
                    middleware.handle(httpRequest))
                return true;
        }
        return false;
    }
}
