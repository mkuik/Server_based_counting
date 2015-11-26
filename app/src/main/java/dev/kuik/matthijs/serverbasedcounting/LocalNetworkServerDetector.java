package dev.kuik.matthijs.serverbasedcounting;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class LocalNetworkServerDetector implements Runnable {

    private static Adapter listener;
    static ExecutorService es;
    static String baseIP;

    static final int timeout = 30;
    static final int threads = 10;
    static final int[] ip_range = {0, 256};
    static final int[] port_range = {0, 200};

    private static boolean stopThread = false;
    private static long test_count = 0;
    private static long total_tests = (ip_range[1] - ip_range[0]);

    public LocalNetworkServerDetector(Adapter adapter) {
        listener = adapter;
    }

    public void setIPDomain(final String ip) {
        baseIP = ip;
    }

    @Override
    public void run() {
        abort();
        stopThread = false;
        test_count = 0;
        es = Executors.newFixedThreadPool(threads);
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    for (int port = port_range[0]; port < port_range[1] && !stopThread; port++) {
                        try {
                            connect(ip, port);
                            Log.e(ip, "connected to port " + Integer.toString(port));
                        } catch (ConnectException e) {
                            listener.OnIPError(ip + " not in network");
                            break;
                        } catch (IOException e) {
                            listener.OnPortError(ip + " at port " + Integer.toString(port) + " is closed");
                        }
                        if (port == port_range[0]) {
                            Log.e(ip, "ip device found in network");
                        }

                    }
                    test_count++;
                    listener.OnProgressUpdate(getProgress());
                }
            });
        }
        es.shutdown();
    }

    public void abort() {
        stopThread = true;
        if (es != null) {
            es.shutdownNow();
            while (!es.isTerminated()) {}
        }
    }

    public void connect(final String ip, final int port) throws IOException {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            listener.foundOpenPort(ip, port);
    }

    public double getProgress() {
        return test_count / (double)total_tests;
    }

    public interface Adapter {
        void foundOpenPort(final String ip, final int port);
        void OnProgressUpdate(double percentage);
        void OnIPError(final String e);
        void OnPortError(final String e);
    }
}