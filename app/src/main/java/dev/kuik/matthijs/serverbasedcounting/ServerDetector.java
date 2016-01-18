package dev.kuik.matthijs.serverbasedcounting;

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
    final String tag = "Find sockets";
    final int timeout = 200;
    final int[] ip_range = {0, 255};
    final int port = 4500;

    public ServerDetector(final String ip) {
        baseIP = ip;
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
            Log.i(tag, e.toString());
        } catch (JSONException e) {
            Log.i(tag, e.toString());
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    testAddress(ip, port);
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