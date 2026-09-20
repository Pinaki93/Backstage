package dev.pinaki.backstage.library.impl.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.impl.http.util.RequestReader;

public final class RequestChain {
    private final List<Middleware> middlewares = new ArrayList<>();
    private final HttpRequest httpRequest;
    private int index;

    public static class Executor {
        private final List<Middleware> middlewares;
        private final List<BasicController> controllers = new CopyOnWriteArrayList<>();

        public static Executor withMiddlewares(List<Middleware> middlewares) {
            return new Executor(middlewares);
        }

        public void addMiddleware(Middleware middleware) {
            middlewares.add(middleware);
        }

        public void addController(BasicController controller) {
            controllers.add(controller);
        }

        public List<BasicController> getControllers() {
            return controllers;
        }

        private Executor(List<Middleware> middlewares) {
            this.middlewares = middlewares;
        }

        public void execute(Socket client) {
            try (Socket socket = client;
                 OutputStream ignored = socket.getOutputStream()) {
                RequestChain
                        .withClient(socket)
                        .withMiddlewares(middlewares)
                        .handle();
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
            httpRequest = new HttpRequest(client, null, null, null, null);
        } else {
            httpRequest = new HttpRequest(client, parts[0], parts[1], parts[2], reader.readHeaders());
        }

        return new RequestChain(httpRequest);
    }

    private RequestChain(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public RequestChain withMiddlewares(List<Middleware> middleware) {
        middlewares.addAll(middleware);
        return this;
    }

    public boolean handle() throws IOException {
        if (index == middlewares.size()) return false;
        return middlewares.get(index++).handle(httpRequest, this);
    }
}
