package dev.pinaki.backstage.library.noop;

import org.junit.Test;

import dev.pinaki.backstage.library.BackstageDelegate;
import dev.pinaki.backstage.library.BasicController;

public class BackstageDelegateTest {
    @Test
    public void supportsInitializationWithoutPages() {
        BackstageDelegate delegate = new BackstageDelegate(null);

        delegate.init();
        delegate.addController(new BasicController("/test") {
            @Override
            public int getHtmlResource() {
                return 1;
            }
        });
    }
}
