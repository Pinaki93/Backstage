package dev.pinaki.backstage.library.impl.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.pinaki.backstage.library.KeyValueController;

public class KeyValueBackstageHttpServerTest {
    private BackstageHttpServer server;

    @Before
    public void startServer() throws IOException {
        server = new BackstageHttpServer(0, path ->
                new ByteArrayInputStream("<h1>Backstage</h1>"
                        .getBytes(StandardCharsets.UTF_8)));
        server.start();
    }

    @After
    public void stopServer() throws IOException {
        server.close();
    }

    @Test
    public void dashboardListsRegisteredKeyValueController() throws IOException {
        server.addController(new TestKeyValueController());

        String response = request("GET / HTTP/1.1\r\n\r\n");

        assertTrue(response.endsWith("</body></html>"));
        assertTrue(response.contains("Preferences"));
        assertTrue(response.contains("href=\"/preferences\""));
    }

    @Test
    public void keyValueApiReadsAndMutatesEntries() throws IOException {
        TestKeyValueController controller = new TestKeyValueController();
        server.addController(controller);

        assertTrue(request("GET /preferences/api HTTP/1.1\r\n\r\n")
                .endsWith("{\"theme\":\"dark\"}"));
        assertTrue(request("POST /preferences/api HTTP/1.1\r\nContent-Length: 20\r\n\r\nkey=size&value=large")
                .startsWith("HTTP/1.1 204 No Content"));
        assertEquals("large", controller.values.get("size"));
        request("PUT /preferences/api HTTP/1.1\r\nContent-Length: 21\r\n\r\nkey=theme&value=light");
        assertEquals("light", controller.values.get("theme"));
        request("DELETE /preferences/api?key=theme HTTP/1.1\r\n\r\n");
        assertFalse(controller.values.containsKey("theme"));
        request("DELETE /preferences/api HTTP/1.1\r\n\r\n");
        assertTrue(controller.values.isEmpty());
    }

    @Test
    public void keyValuePageAndValidationAreServed() throws IOException {
        server.addController(new TestKeyValueController());

        assertTrue(request("GET /preferences HTTP/1.1\r\n\r\n")
                .contains("new EventSource(base+'/events')"));
        assertTrue(request("POST /preferences/api HTTP/1.1\r\nContent-Length: 7\r\n\r\nvalue=x")
                .startsWith("HTTP/1.1 400 Bad Request"));
    }

    @Test
    public void eventStreamReflectsCallbackUpdates() throws IOException {
        TestKeyValueController controller = new TestKeyValueController();
        server.addController(controller);
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             OutputStream output = socket.getOutputStream();
             BufferedReader input = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(2_000);
            output.write("GET /preferences/events HTTP/1.1\r\n\r\n"
                    .getBytes(StandardCharsets.US_ASCII));
            output.flush();
            String line;
            while (!(line = input.readLine()).isEmpty()) {
                // Consume response headers.
            }
            assertEquals("data: {\"theme\":\"dark\"}", input.readLine());
            assertEquals("", input.readLine());

            controller.update("theme", "light");

            assertEquals("data: {\"theme\":\"light\"}", input.readLine());
        }
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

    private static final class TestKeyValueController extends KeyValueController {
        private final Map<String, String> values = new LinkedHashMap<>();
        private final TestSource source;

        private TestKeyValueController() {
            this(new TestSource(new LinkedHashMap<>(
                    java.util.Collections.singletonMap("theme", "dark"))));
        }

        private TestKeyValueController(TestSource source) {
            super("Preferences", "/preferences", source);
            this.source = source;
            values.putAll(source.initial);
        }

        @Override public void create(String key, String value) { values.put(key, value); emit(); }
        @Override public void update(String key, String value) { values.put(key, value); emit(); }
        @Override public void delete(String key) { values.remove(key); emit(); }
        @Override public void clear() { values.clear(); emit(); }
        private void emit() { source.callback.onChanged(new LinkedHashMap<>(values)); }
    }

    private static final class TestSource implements KeyValueController.EntriesSource {
        private final Map<String, String> initial;
        private KeyValueController.EntriesCallback callback;

        private TestSource(Map<String, String> initial) { this.initial = initial; }

        @Override public void subscribe(KeyValueController.EntriesCallback callback) {
            this.callback = callback;
            callback.onChanged(initial);
        }
    }
}
