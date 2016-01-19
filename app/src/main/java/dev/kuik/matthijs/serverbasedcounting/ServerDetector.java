package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class ServerDetector extends AsyncTask<Void, ServerAddress, Void> {

    String baseIP;
    final String tag = "ServerDetector";
    final int timeout = 300;
    final int[] ip_range = {0, 255};
    final int port = 4500;

    public ServerDetector(Context context) {
        final int ip = ServerAddress.getIP(context);
        baseIP = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
    }

    public void testAddress(final String ip, final int port) {
        if (isCancelled()) return;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            Log.i(tag, "connected to " + ip + " " + port);
            final JSONObject meta = new JSONObject(read(socket));
            final int nServers = meta.getInt("servers_size");
            for (int i = 0; i != nServers; ++i) {
                final JSONObject json = meta.getJSONObject("server#" + i);
                ServerAddress server = new ServerAddress(ip, json.getInt("port"),
                        json.getString("desc"));
                server.setColor1(Color.parseColor(json.getString("color1")));
                server.setColor2(Color.parseColor(json.getString("color2")));
                server.setIcon(Global.toBitmap(json.getString("icon")));
                publishProgress(server);
            }
        } catch (IOException e) {
            Log.i(tag, "Can't connect to " + ip + ":" + port);
        } catch (JSONException e) {
            Log.e(tag, e.toString());
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        if (Global.getHost() != null && Global.getHost().ip.compareTo("") != 0) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    testAddress(Global.getHost().ip, port);
                }
            });
        }
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            if (Global.getHost().ip.compareTo(ip) != 0) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        testAddress(ip, port);
                    }
                });
            }
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) { }
        } catch (InterruptedException ignored) {

        }
        return null;
    }

    public String read(final Socket socket) throws IOException {
        if (socket != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"), 8192);
            String str;
            StringBuilder sb = new StringBuilder(8192);
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        }
        return null;
    }
}