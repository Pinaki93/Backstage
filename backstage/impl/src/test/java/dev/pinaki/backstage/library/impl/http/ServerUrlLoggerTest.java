package dev.pinaki.backstage.library.impl.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class ServerUrlLoggerTest {
    @Test
    public void createsSortedUrlsForNonLoopbackIpv4Addresses() throws Exception {
        List<String> urls = ServerUrlLogger.urls(Arrays.asList(
                InetAddress.getByName("192.168.1.20"),
                InetAddress.getByName("127.0.0.1"),
                InetAddress.getByName("10.0.0.4"),
                InetAddress.getByName("2001:db8::1")), 8317);

        assertEquals(Arrays.asList(
                "http://10.0.0.4:8317/",
                "http://192.168.1.20:8317/"), urls);
    }
}
