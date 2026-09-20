package dev.pinaki.backstage.library;

import android.content.Context;

public final class BackstageDelegate implements Backstage.Delegate {
    public BackstageDelegate(Context context) {
        // The no-op implementation deliberately does not retain the application context.
    }

    @Override
    public void init() {
    }

    @Override
    public void addController(BasicController controller) {
    }
}
