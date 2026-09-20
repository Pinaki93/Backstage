package dev.pinaki.backstage.library.impl.http;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

final class ServerUrlLogger {
    private static final String TAG = "Backstage";

    private ServerUrlLogger() {
    }

    static void log(int port) {
        try {
            for (String url : urls(networkAddresses(), port)) {
                Log.i(TAG, "Connect to Backstage at " + url);
            }
        } catch (SocketException exception) {
            Log.w(TAG, "Backstage started, but its network URLs could not be determined", exception);
        }
    }

    static List<String> urls(Iterable<InetAddress> addresses, int port) {
        List<String> urls = new ArrayList<>();
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address && !address.isLoopbackAddress()
                    && !address.isAnyLocalAddress()) {
                urls.add("http://" + address.getHostAddress() + ":" + port + "/");
            }
        }
        Collections.sort(urls);
        return urls;
    }

    private static List<InetAddress> networkAddresses() throws SocketException {
        List<InetAddress> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) return addresses;
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isUp() || networkInterface.isLoopback()) continue;
            Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
            while (interfaceAddresses.hasMoreElements()) {
                addresses.add(interfaceAddresses.nextElement());
            }
        }
        return addresses;
    }
}
