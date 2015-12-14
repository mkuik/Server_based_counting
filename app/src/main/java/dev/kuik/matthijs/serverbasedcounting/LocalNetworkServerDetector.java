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
import java.util.concurrent.TimeUnit;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class LocalNetworkServerDetector extends AsyncTask<String, String, String> {

    private static Adapter listener;
    static ExecutorService es;
    static String baseIP;
    static InetAddress address;

    private final String tag = "Server locate";
    static final int timeout = 500;
    static int threads = 1;
    static final int thread_scale = 3;
    static final int[] ip_range = {0, 256};
    static final int[] port_range = {4444, 4445};
    private static boolean stopThread = false;

    public LocalNetworkServerDetector(Adapter adapter, final String ip) {
        listener = adapter;
        int cpus = Runtime.getRuntime().availableProcessors();
        threads = cpus * thread_scale;
        threads = (threads > 0 ? threads : 1);
        baseIP = ip;
    }

    @Override
    protected String doInBackground(String... params) {
        es = Executors.newFixedThreadPool(threads);
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            testSocketPorts(ip);
        }
        es.shutdown();
        try {
            while (!es.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Log.e(tag, e.toString());
        }
        return null;
    }

    public void testSocketPorts(final String ip) {
        es.execute(new Runnable() {
            @Override
            public void run() {
                Integer port = port_range[0];
                for (; port < port_range[1] && !stopThread; port++) {
                    publishProgress(ip + " " + Integer.toString(port));
                    try {
                        connect(ip, port);
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    public void connect(final String ip, final int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout);
        socket.close();
        listener.foundOpenPort(new ServerAddress(ip, port));
    }

    public interface Adapter {
        void foundOpenPort(final ServerAddress address);
        void OnStatusReport(final String e);
    }
}