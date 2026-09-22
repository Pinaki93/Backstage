package dev.pinaki.backstage.library;

import android.util.Log;

/** Logging used by Backstage and available to its host application. */
public final class BackstageLog {
    private static volatile Level level = Level.DEBUG;
    private static volatile Delegate delegate = new AndroidDelegate();

    private BackstageLog() {
    }

    public static void setLevel(Level level) {
        if (level == null) throw new IllegalArgumentException("level must not be null");
        BackstageLog.level = level;
    }

    public static Level getLevel() {
        return level;
    }

    public static void setDelegate(Delegate delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate must not be null");
        BackstageLog.delegate = delegate;
    }

    public static void resetDelegate() {
        delegate = new AndroidDelegate();
    }

    public static void d(String tag, String message) {
        log(Level.DEBUG, tag, message, null);
    }

    public static void i(String tag, String message) {
        log(Level.INFO, tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        log(Level.WARN, tag, message, throwable);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Level.ERROR, tag, message, throwable);
    }

    public static void a(String tag, String message, Throwable throwable) {
        log(Level.ASSERT, tag, message, throwable);
    }

    private static void log(Level messageLevel, String tag, String message, Throwable throwable) {
        if (enabled(messageLevel)) delegate.log(messageLevel, tag, message, throwable);
    }

    private static boolean enabled(Level messageLevel) {
        return messageLevel.priority >= level.priority;
    }

    public interface Delegate {
        void log(Level level, String tag, String message, Throwable throwable);
    }

    private static final class AndroidDelegate implements Delegate {
        @Override public void log(Level level, String tag, String message, Throwable throwable) {
            switch (level) {
                case DEBUG: Log.d(tag, message); break;
                case INFO: Log.i(tag, message); break;
                case WARN: Log.w(tag, message, throwable); break;
                case ERROR: Log.e(tag, message, throwable); break;
                case ASSERT: Log.wtf(tag, message, throwable); break;
            }
        }
    }

    public enum Level {
        DEBUG(0), INFO(1), WARN(2), ERROR(3), ASSERT(4);

        private final int priority;

        Level(int priority) {
            this.priority = priority;
        }
    }
}
