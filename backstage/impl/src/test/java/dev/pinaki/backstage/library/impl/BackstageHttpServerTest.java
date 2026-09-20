package dev.pinaki.backstage.library.impl.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import dev.pinaki.backstage.library.BasicController;

public class BackstageHttpServerTest {
    private BackstageHttpServer server;

    @Before
    public void startServer() throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        files.put("index.html", "<h1>Backstage</h1>".getBytes(StandardCharsets.UTF_8));
        files.put("assets/app.js", "console.log('ready')".getBytes(StandardCharsets.UTF_8));
        server = new BackstageHttpServer(0, path -> {
            byte[] contents = files.get(path);
            if (contents == null) throw new IOException("missing");
            return new ByteArrayInputStream(contents);
        });
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.close();
    }

    @Test
    public void rootServesIndexHtml() throws IOException {
        String response = request("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.contains("Content-Type: text/html; charset=utf-8\r\n"));
        assertTrue(response.endsWith("<h1>Backstage</h1>"));
    }

    @Test
    public void registeredControllerServesHtmlResource() throws IOException {
        server.close();
        server = new BackstageHttpServer(0,
                path -> new ByteArrayInputStream("asset fallback".getBytes(StandardCharsets.UTF_8)),
                resourceId -> new ByteArrayInputStream(
                        "<h1>Sample controller</h1>".getBytes(StandardCharsets.UTF_8)));
        server.addController(new BasicController("/") {
            @Override
            public int getHtmlResource() {
                return 7;
            }
        });
        server.start();

        String response = request("GET / HTTP/1.1\r\n\r\n");

        assertTrue(response.contains("Content-Type: text/html; charset=utf-8\r\n"));
        assertTrue(response.endsWith("<h1>Sample controller</h1>"));
    }

    @Test
    public void unhandledControllerPathFallsBackToAsset() throws IOException {
        server.addController(new BasicController("/controller") {
            @Override
            public int getHtmlResource() {
                return 7;
            }
        });

        String response = request("GET / HTTP/1.1\r\n\r\n");

        assertTrue(response.endsWith("<h1>Backstage</h1>"));
    }

    @Test
    public void servesNestedAssetAndIgnoresQueryString() throws IOException {
        String response = request("GET /assets/app.js?v=1 HTTP/1.1\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.contains("Content-Type: text/javascript; charset=utf-8\r\n"));
        assertTrue(response.endsWith("console.log('ready')"));
    }

    @Test
    public void missingAssetReturnsNotFound() throws IOException {
        String response = request("GET /missing.css HTTP/1.1\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 404 Not Found\r\n"));
        assertTrue(response.endsWith("Not Found"));
    }

    @Test
    public void pathTraversalIsRejected() throws IOException {
        assertTrue(request("GET /%2e%2e/secret HTTP/1.1\r\n\r\n")
                .startsWith("HTTP/1.1 400 Bad Request\r\n"));
    }

    @Test
    public void malformedRequestReturnsBadRequest() throws IOException {
        assertTrue(request("GET /\r\n\r\n").startsWith("HTTP/1.1 400 Bad Request\r\n"));
    }

    @Test
    public void malformedEncodingReturnsBadRequest() throws IOException {
        assertTrue(request("GET /bad%zz HTTP/1.1\r\n\r\n")
                .startsWith("HTTP/1.1 400 Bad Request\r\n"));
    }

    @Test
    public void headReturnsHeadersWithoutBody() throws IOException {
        String response = request("HEAD / HTTP/1.1\r\n\r\n");
        assertTrue(response.contains("Content-Length: 18\r\n"));
        assertTrue(response.endsWith("\r\n\r\n"));
        assertFalse(response.endsWith("<h1>Backstage</h1>"));
    }

    @Test
    public void unsupportedMethodReturnsMethodNotAllowed() throws IOException {
        String response = request("POST / HTTP/1.1\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 405 Method Not Allowed\r\n"));
        assertTrue(response.contains("Allow: GET, HEAD\r\n"));
    }

    @Test
    public void errorResponseDoesNotPreventLaterSuccessResponse() throws IOException {
        assertTrue(request("POST / HTTP/1.1\r\n\r\n")
                .startsWith("HTTP/1.1 405 Method Not Allowed\r\n"));

        assertTrue(request("GET / HTTP/1.1\r\n\r\n")
                .startsWith("HTTP/1.1 200 OK\r\n"));
    }

    @Test
    public void closeStopsServer() throws IOException {
        server.close();
        assertEquals(-1, server.getPort());
    }

    @Test
    public void startIsIdempotent() throws IOException {
        int port = server.getPort();
        server.start();

        assertEquals(port, server.getPort());
        assertTrue(request("GET / HTTP/1.1\r\n\r\n").startsWith("HTTP/1.1 200 OK\r\n"));
    }

    @Test
    public void serverCanRestartAfterClose() throws IOException {
        server.close();
        server.start();

        assertTrue(server.getPort() > 0);
        assertTrue(request("GET / HTTP/1.1\r\n\r\n").startsWith("HTTP/1.1 200 OK\r\n"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidPort() {
        new BackstageHttpServer(65_536, path -> new ByteArrayInputStream(new byte[0]));
    }

    private String request(String request) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             OutputStream output = socket.getOutputStream();
             InputStream input = socket.getInputStream()) {
            output.write(request.getBytes(StandardCharsets.US_ASCII));
            output.flush();
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) response.write(buffer, 0, count);
            return response.toString(StandardCharsets.UTF_8.name());
        }
    }
}
