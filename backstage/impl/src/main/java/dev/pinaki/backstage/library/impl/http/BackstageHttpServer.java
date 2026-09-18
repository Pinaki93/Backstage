package dev.pinaki.backstage.library.impl.http;

import android.content.res.AssetManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.pinaki.backstage.library.impl.http.middlewares.AssetMiddleware;
import dev.pinaki.backstage.library.impl.http.middlewares.ErrorMiddleware;

/**
 * A small HTTP/1.1 server that serves the Backstage web application from assets.
 */
public final class BackstageHttpServer implements Closeable {
    public static final int DEFAULT_PORT = 8317;

    private final int requestedPort;
    private ServerSocket serverSocket;
    private ExecutorService clients;
    private final RequestChain.Executor requestChainExecutor;

    /**
     * Serves files below {@code backstage/} in the application's assets directory.
     */
    public BackstageHttpServer(AssetManager assetManager) {
        this(assetManager, DEFAULT_PORT);
    }

    /**
     * Serves files below {@code backstage/} on {@code port}. Use 0 to select a free port.
     */
    public BackstageHttpServer(AssetManager assetManager, int port) {
        this(port, path -> assetManager.open("backstage/" + path, AssetManager.ACCESS_STREAMING));
    }

    BackstageHttpServer(int port, AssetSource assets) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        requestedPort = port;
        requestChainExecutor = RequestChain.Executor.withMiddlewares(
                Arrays.asList(new ErrorMiddleware(), new AssetMiddleware(assets)));
    }

    /**
     * Starts listening on the loopback interface. Calling this twice has no effect.
     */
    public synchronized void start() throws IOException {
        if (serverSocket != null) return;

        ServerSocket socket = new ServerSocket(requestedPort, 50, InetAddress.getLoopbackAddress());
        ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "Backstage HTTP client");
            thread.setDaemon(true);
            return thread;
        });
        serverSocket = socket;
        clients = executor;
        Thread acceptThread = new Thread(() -> acceptConnections(socket, executor),
                "Backstage HTTP server");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    /**
     * Returns the bound port, or {@code -1} while the server is stopped.
     */
    public synchronized int getPort() {
        return serverSocket == null ? -1 : serverSocket.getLocalPort();
    }

    private void acceptConnections(ServerSocket socket, ExecutorService executor) {
        while (!socket.isClosed()) {
            try {
                final Socket client = socket.accept();
                executor.execute(() -> requestChainExecutor.execute(client));
            } catch (SocketException ignored) {
                // close() wakes the accept thread by closing the server socket.
            } catch (IOException ignored) {
                // A failed connection must not stop later requests.
            }
        }
    }

    /**
     * Stops the server and releases its socket and worker threads.
     */
    @Override
    public synchronized void close() throws IOException {
        ServerSocket socket = serverSocket;
        ExecutorService executor = clients;
        serverSocket = null;
        clients = null;
        if (socket != null) socket.close();
        if (executor != null) executor.shutdownNow();
    }

    public interface AssetSource {
        InputStream open(String path) throws IOException;
    }
}
