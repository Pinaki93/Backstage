package dev.pinaki.backstage.library.impl.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dev.pinaki.backstage.library.BasicController;

public class BackstageContainerTest {
    @Test
    public void controllersAreScopedToTheContainer() {
        BackstageContainer first = new BackstageContainer();
        BackstageContainer second = new BackstageContainer();

        first.controllerFactory().addController(new BasicController("/first") {
            @Override public int getHtmlResource() { return 1; }
        });

        assertEquals(1, first.controllerFactory().basicControllers().size());
        assertTrue(second.controllerFactory().basicControllers().isEmpty());
    }
}
