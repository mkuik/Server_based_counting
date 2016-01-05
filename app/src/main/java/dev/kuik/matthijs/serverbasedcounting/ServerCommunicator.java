package dev.kuik.matthijs.serverbasedcounting;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Matthijs Kuik on 1-12-2015.
 */
public class ServerCommunicator extends AsyncTask<String, Integer, String> {

    private final String tag = "Server contact";
    private ServerAddress address;
    private Socket socket;

    public ServerCommunicator(ServerAddress address) {
        this.address = address;
    }

    public void write(final String message) throws IOException {
        if (socket != null) {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter, 8192);
            PrintWriter writer = new PrintWriter(bufferedWriter, true);
            writer.println(message);
            writer.flush();
        }
    }

    public String read() throws IOException {
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

    @Override
    protected String doInBackground(String... params) {

        String response = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address.ip, address.port), 3000);
            write(params[0])           ;
            socket.shutdownOutput();
            response = read();
            socket.close();
        } catch (IOException e) {
            Log.e(tag, e.toString());
        }
        return response;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Global.notifyResponse(s);
        Global.setHostConnectionActive(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Global.setHostConnectionActive(true);
    }
}

