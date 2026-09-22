package dev.pinaki.backstage.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BasicControllerTest {
    @Test
    public void exposesPathAndHtmlResource() {
        BasicController controller = new BasicController("/") {
            @Override
            public int getHtmlResource() {
                return 123;
            }
        };

        assertEquals("/", controller.getPath());
        assertEquals(123, controller.getHtmlResource());
        assertNull(controller.getHtml());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRelativePath() {
        new BasicController("relative") {
            @Override
            public int getHtmlResource() {
                return 123;
            }
        };
    }
}
