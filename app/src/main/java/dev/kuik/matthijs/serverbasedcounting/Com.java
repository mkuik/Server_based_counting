package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by Matthijs on 13/08/16.
 */
public class Com {

    private static Set<Adapter> listeners = new HashSet<>();

    interface Adapter {
        void comPing();
        void comFailed();
    }

    public static void addListener(Adapter adapter) {
        listeners.add(adapter);
    }

    public static void removeListener(Adapter adapter) {
        listeners.remove(adapter);
    }

    public static void notifyPing() {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.comPing();
        }
    }

    public static void notifyFail() {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.comFailed();
        }
    }

    public static User getAccount() {
        return Global.getUser();
    }

    public static void setAccount(User account) {
        Global.setUser(account);
    }

    public static InetSocketAddress getAddress() {
        return Global.getHost().getAddress();
    }

    public static class Sync extends AsyncTask<JSONObject, Void, JSONObject> {

        private static final String TAG = "Sync";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject in = null;
            try {
                final JSONObject out = params.length != 0 ? params[0] : new JSONObject();
                out.put("ACCOUNT", getAccount().toJSON());
                synchronized (getAddress()) {
                    in = com(getAddress(), out, 5000);
                }
            } catch (JSONException | IOException | NullPointerException e) {
                Log.d(TAG, e.toString());
            }
            return in;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {
                notifyPing();
            } else {
                notifyFail();
            }
        }
    }

    public static class Status extends Sync {

        private static final String TAG = "Status";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject in = null;
            try {
                final JSONObject out = params.length != 0 ? params[0] : new JSONObject();
                out.put("CMD", "STATUS");
                out.put("ID", Global.getHost().getId());
                in = super.doInBackground(out);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            return in;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {
                try {
                    onStatus(jsonObject.getInt("COUNT"), jsonObject.getInt("MAX"));
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }

        protected void onStatus(final int count, final int max) {

        }
    }

    public static class Edit extends Status {

        private static final String TAG = "Edit";
        private int value;

        public Edit(int value) {
            this.value = value;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject in = null;
            try {
                final JSONObject out = params.length != 0 ? params[0] : new JSONObject();
                out.put("EDIT", value);
                in = super.doInBackground(out);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            return in;
        }
    }

    public static class DownloadIcon extends AsyncTask<Void, Void, Bitmap> {

        private static final String TAG = "DownloadIcon";
        private Counter counter;
        private Context context;

        public DownloadIcon(Context context, Counter counter) {
            this.context = context;
            this.counter = counter;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            File file = new File(context.getCacheDir(), counter.getIcon());
            Log.d(TAG, "GET " + file.getPath());
            if (file.exists()) bitmap = BitmapFactory.decodeFile(file.getPath());
            if (bitmap == null) {
                try {
                        Log.d(TAG, "FROM SERVER ");
                        download(counter.getAddress(), file, counter.getId(), 5000);

                    bitmap = BitmapFactory.decodeFile(file.getPath());
                } catch (JSONException | IOException | NullPointerException e) {
                    Log.d(TAG, e.toString());
                }
            }
            return bitmap;
        }
    }

    public static class LoadImage extends Com.DownloadIcon {

        ImageView view;

        public LoadImage(Context context, Counter counter, ImageView view) {
            super(context, counter);
            this.view = view;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            view.setImageBitmap(bitmap);
        }
    }

    public static class Meta extends AsyncTask<JSONObject, Void, JSONObject> {

        private static final String TAG = "Meta";
        private String ip;
        private int port;

        public Meta(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject in = null;
            try {
                final JSONObject out = params.length != 0 ? params[0] : new JSONObject();
                out.put("ACCOUNT", getAccount().toJSON());
                out.put("CMD", "META");
                synchronized (ip) {
                    in = com(new InetSocketAddress(ip, port), out, 100);
                }
            } catch (ConnectException e) {
                Log.d(TAG, "CAN'T CONNECT TO " + ip.toString());
            } catch (IOException e) {
                Log.v(TAG, "CAN'T CONNECT TO " + ip.toString());
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            return in;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject == null) return;
            Log.d(TAG, jsonObject.toString());
            User account = null;
            try {
                account = new User(jsonObject.optJSONObject("ACCOUNT"));
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            try {
                JSONArray array = jsonObject.getJSONArray("COUNTERS");
                for (int i = 0; i != array.length(); ++i) {
                    Counter counter = new Counter(array.getJSONObject(i));
                    counter.setAddress(ip, port);
                    onCounter(account, counter);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

        }

        protected void onCounter(User account, Counter counter) {

        }
    }

    protected static JSONObject com(InetSocketAddress server, JSONObject message, int timeout) throws IOException {
        if (server == null) return null;
        Socket socket = new Socket();
        socket.connect(server, timeout);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        write(out, message.toString());
        socket.shutdownOutput();
        String response_str = read(in);
        out.close();
        in.close();
        socket.close();
        JSONObject response = null;
        try {
            response = new JSONObject(response_str);
        } catch (JSONException e) {
            Log.e("com()", e.toString());
        }
        return response;
    }

    protected static void write(OutputStream out, final String message) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw, 8192);
        PrintWriter pw = new PrintWriter(bw);
        pw.println(message);
        pw.flush();
    }

    protected static String read(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8192);
        String str;
        StringBuilder sb = new StringBuilder(8192);
        while ((str = r.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }

    protected static void download(InetSocketAddress server, File file, int ID, int timeout) throws IOException, JSONException {
        Socket socket = new Socket();
        socket.connect(server, timeout);

        JSONObject json = new JSONObject();
        json.put("ACCOUNT", getAccount().toJSON());
        json.put("CMD", "DOWNLOAD");
        json.put("ID", ID);
        Log.d("download()", json.toString());
        OutputStream out = socket.getOutputStream();
        out.write((json.toString() + "\n").getBytes());
        socket.shutdownOutput();

        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        InputStream in = socket.getInputStream();
        IOUtils.copy(in, fos);

        in.close();
        out.close();
        socket.close();
    }
}
