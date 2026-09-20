package dev.pinaki.backstage.library.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import dev.pinaki.backstage.library.BasicController;

public class BackstageDelegateTest {
    @Test
    public void controllerExposesPathAndHtmlResource() {
        BasicController controller = new BasicController("/test") {
            @Override
            public int getHtmlResource() {
                return 42;
            }
        };

        assertEquals("/test", controller.getPath());
        assertEquals(42, controller.getHtmlResource());
    }
}
