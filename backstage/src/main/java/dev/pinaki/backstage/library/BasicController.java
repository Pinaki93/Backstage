package dev.pinaki.backstage.library;

/** Maps an HTTP path to an HTML resource bundled with the host application. */
public abstract class BasicController {
    private final String path;

    protected BasicController(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        this.path = path;
    }

    public final String getPath() {
        return path;
    }

    /** Returns the id of a raw HTML resource. */
    public abstract int getHtmlResource();

    /** Returns generated HTML, or {@code null} to use {@link #getHtmlResource()}. */
    public String getHtml() {
        return null;
    }
}
