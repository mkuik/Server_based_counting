package dev.kuik.matthijs.serverbasedcounting;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class LocalNetworkServerDetector extends AsyncTask<String, ServerAddress, Void> {

    String baseIP;
    final String tag = "Find sockets";
    final int timeout = 300;
    final int[] ip_range = {100, 120};
    final int[] port_range = {4500, 4501};

    public LocalNetworkServerDetector(final String ip) {
        baseIP = ip;
    }

    @Override
    protected Void doInBackground(String... params) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    testSocketPorts(ip);
                }
            });
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) { }
        } catch (InterruptedException ignored) {

        }
        Log.e("ip detector", "end");
        return null;
    }

    public void testSocketPorts(final String ip) {
        Log.i(tag, ip);
        if (isCancelled()) return;
        for (final Integer port : port_range) {
            try {
                connect(ip, port);
            } catch (IOException ignored) {
            }
        }
    }

    public void connect(final String ip, final int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout);
        socket.close();
        publishProgress(new ServerAddress(ip, port, null));
    }
}