package dev.pinaki.backstage.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackstageLogTest {
    private final List<Entry> entries = new ArrayList<>();
    private final RuntimeException failure = new RuntimeException("failure");

    @Before
    public void collectLogs() {
        BackstageLog.setDelegate((level, tag, message, throwable) ->
                entries.add(new Entry(level, tag, message, throwable)));
    }

    @After
    public void resetLogger() {
        BackstageLog.setLevel(BackstageLog.Level.DEBUG);
        BackstageLog.resetDelegate();
    }

    @Test
    public void debugLevelEmitsEveryLog() {
        assertLogsAt(BackstageLog.Level.DEBUG, BackstageLog.Level.DEBUG,
                BackstageLog.Level.INFO, BackstageLog.Level.WARN,
                BackstageLog.Level.ERROR, BackstageLog.Level.ASSERT);
    }

    @Test
    public void infoLevelFiltersDebug() {
        assertLogsAt(BackstageLog.Level.INFO, BackstageLog.Level.INFO,
                BackstageLog.Level.WARN, BackstageLog.Level.ERROR,
                BackstageLog.Level.ASSERT);
    }

    @Test
    public void warnLevelFiltersDebugAndInfo() {
        assertLogsAt(BackstageLog.Level.WARN, BackstageLog.Level.WARN,
                BackstageLog.Level.ERROR, BackstageLog.Level.ASSERT);
    }

    @Test
    public void errorLevelEmitsErrorAndAssert() {
        assertLogsAt(BackstageLog.Level.ERROR, BackstageLog.Level.ERROR,
                BackstageLog.Level.ASSERT);
    }

    @Test
    public void assertLevelEmitsOnlyAssert() {
        assertLogsAt(BackstageLog.Level.ASSERT, BackstageLog.Level.ASSERT);
    }

    @Test
    public void everyLoggingMethodForwardsItsPayload() {
        emitEveryLevel();

        assertEntry(0, BackstageLog.Level.DEBUG, "debug", null);
        assertEntry(1, BackstageLog.Level.INFO, "info", null);
        assertEntry(2, BackstageLog.Level.WARN, "warn", failure);
        assertEntry(3, BackstageLog.Level.ERROR, "error", failure);
        assertEntry(4, BackstageLog.Level.ASSERT, "assert", failure);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullLevel() {
        BackstageLog.setLevel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullDelegate() {
        BackstageLog.setDelegate(null);
    }

    private void assertLogsAt(BackstageLog.Level configured,
                              BackstageLog.Level... expected) {
        BackstageLog.setLevel(configured);
        assertEquals(configured, BackstageLog.getLevel());
        emitEveryLevel();

        List<BackstageLog.Level> actual = new ArrayList<>();
        for (Entry entry : entries) actual.add(entry.level);
        assertEquals(Arrays.asList(expected), actual);
    }

    private void emitEveryLevel() {
        BackstageLog.d("tag", "debug");
        BackstageLog.i("tag", "info");
        BackstageLog.w("tag", "warn", failure);
        BackstageLog.e("tag", "error", failure);
        BackstageLog.a("tag", "assert", failure);
    }

    private void assertEntry(int index, BackstageLog.Level level, String message,
                             Throwable throwable) {
        Entry entry = entries.get(index);
        assertEquals(level, entry.level);
        assertEquals("tag", entry.tag);
        assertEquals(message, entry.message);
        if (throwable == null) assertNull(entry.throwable);
        else assertSame(throwable, entry.throwable);
    }

    private static final class Entry {
        private final BackstageLog.Level level;
        private final String tag;
        private final String message;
        private final Throwable throwable;

        private Entry(BackstageLog.Level level, String tag, String message,
                      Throwable throwable) {
            this.level = level;
            this.tag = tag;
            this.message = message;
            this.throwable = throwable;
        }
    }
}
