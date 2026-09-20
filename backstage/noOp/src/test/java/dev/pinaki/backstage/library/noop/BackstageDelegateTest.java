package dev.pinaki.backstage.library.noop;

import org.junit.Test;

import dev.pinaki.backstage.library.BackstageDelegate;
import dev.pinaki.backstage.library.Page;

public class BackstageDelegateTest {
    @Test
    public void supportsInitializationWithoutPages() {
        BackstageDelegate delegate = new BackstageDelegate();

        delegate.init();
        delegate.addPage(new Page("test") { });
    }
}
