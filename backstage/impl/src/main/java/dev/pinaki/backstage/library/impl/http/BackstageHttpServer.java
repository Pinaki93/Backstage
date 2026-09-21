package dev.pinaki.backstage.library.impl.http;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.pinaki.backstage.library.impl.di.BackstageContainer;
import dev.pinaki.backstage.library.impl.http.middlewares.AssetMiddleware;
import dev.pinaki.backstage.library.impl.http.middlewares.ControllerMiddleware;
import dev.pinaki.backstage.library.impl.http.middlewares.ErrorMiddleware;
import dev.pinaki.backstage.library.BasicController;
import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.controller.DashboardController;
import dev.pinaki.backstage.library.impl.http.middlewares.KeyValueControllerMiddleware;

/**
 * A small HTTP/1.1 server that serves the Backstage web application from assets.
 */
public final class BackstageHttpServer implements Closeable {
    public static final int DEFAULT_PORT = 8317;

    private final int requestedPort;
    private final boolean logServerUrls;
    private ServerSocket serverSocket;
    private ExecutorService clients;
    private final RequestChain.Executor requestChainExecutor;
    private final BackstageContainer container;

    /**
     * Serves files below {@code backstage/} in the application's assets directory.
     */
    public BackstageHttpServer(AssetManager assetManager) {
        this(assetManager, DEFAULT_PORT);
    }

    public BackstageHttpServer(Context context) {
        this(DEFAULT_PORT,
                path -> context.getAssets().open("backstage/" + path, AssetManager.ACCESS_STREAMING),
                resourceId -> context.getResources().openRawResource(resourceId), true);
    }

    /**
     * Serves files below {@code backstage/} on {@code port}. Use 0 to select a free port.
     */
    public BackstageHttpServer(AssetManager assetManager, int port) {
        this(port, path -> assetManager.open("backstage/" + path, AssetManager.ACCESS_STREAMING),
                resourceId -> {
                    throw new IOException("No resource source configured");
                }, true);
    }

    BackstageHttpServer(int port, AssetSource assets) {
        this(port, assets,
                resourceId -> {
                    throw new IOException("No resource source configured");
                }, false);
    }

    BackstageHttpServer(int port, AssetSource assets,
                        ControllerMiddleware.ResourceSource resources) {
        this(port, assets, resources, false);
    }

    private BackstageHttpServer(int port, AssetSource assets,
                                ControllerMiddleware.ResourceSource resources,
                                boolean logServerUrls) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        requestedPort = port;
        this.logServerUrls = logServerUrls;
        container = new BackstageContainer();
        requestChainExecutor = RequestChain.Executor.getInstance()
                .addMiddleware(new KeyValueControllerMiddleware(container))
                .addMiddleware(new ErrorMiddleware())
                .addMiddleware(new ControllerMiddleware(container, resources))
                .addMiddleware(new DashboardController(container))
                .addMiddleware(new AssetMiddleware(assets));
    }

    public void addController(BasicController controller) {
        container.controllerFactory().addController(controller);
    }

    public void addController(KeyValueController controller) {
        container.controllerFactory().addKeyValueController(controller);
    }

    /**
     * Starts listening on every network interface. Calling this twice has no effect.
     */
    public synchronized void start() throws IOException {
        if (serverSocket != null) return;

        ServerSocket socket = new ServerSocket(requestedPort, 50, null);
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
        if (logServerUrls) ServerUrlLogger.log(socket.getLocalPort());
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
