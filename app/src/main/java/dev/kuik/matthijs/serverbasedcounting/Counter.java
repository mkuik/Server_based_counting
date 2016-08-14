package dev.kuik.matthijs.serverbasedcounting;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

/**
 * Created by Matthijs on 13/08/16.
 */
public class Counter {

    int counter;
    int max;
    int id = -1;
    String color1;
    String color2;
    String icon;
    String hostname;
    int port;
    String password;

    public Counter(int counter, int max, String color1, String color2, String icon) {
        this.counter = counter;
        this.max = max;
        this.color1 = color1;
        this.color2 = color2;
        this.icon = icon;
    }

    public Counter(final JSONObject json) throws JSONException {
        this.counter = json.getInt("count");
        this.max = json.getInt("max");
        this.color1 = json.getString("color1");
        this.color2 = json.getString("color2");
        this.icon = json.getString("icon");
        this.id = json.getInt("ID");
        if (json.has("hostname") && json.has("port")) {
            hostname = json.getString("hostname");
            port = json.getInt("port");
        }
        if (json.has("password")) this.password = json.getString("password");
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("color1", getColor1());
        json.put("color2", getColor2());
        json.put("icon", getIcon());
        json.put("count", getCounter());
        json.put("max", getMax());
        json.put("hostname", hostname);
        json.put("port", port);
        json.put("ID", getId());
        return json;
    }

    public String getColor1() {
        return color1;
    }

    public String getColor2() {
        return color2;
    }

    public int getCounter() {
        return counter;
    }

    public String getIcon() {
        return icon;
    }

    public int getMax() {
        return max;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public InetSocketAddress getAddress() {
        return new InetSocketAddress(hostname, port);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setAddress(InetSocketAddress address) {
        this.hostname = address.getHostString();
        this.port = address.getPort();
    }

    public void setAddress(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setIcon(final Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            setIcon(Base64api7.encodeToString(byteArray, Base64.DEFAULT));
        }
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Bitmap getIconBitmap() {
        try {
            byte[] bytes;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                bytes = Base64.decode(getIcon(), Base64.DEFAULT);
            } else {
                bytes = Base64api7.decode(getIcon(), Base64api7.DEFAULT);
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
            Log.e("Global.toBitmap", e.toString());
        }
        return null;
    }

    public static int getIP(Context context) {
        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiMgr.getConnectionInfo().getIpAddress();
    }

    public static String getIPString(Context context) {
        final int ip = getIP(context);
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 32 & 0xff));
    }

    public static boolean validIP(final String ip) {
        try {
            if ( ip == null || ip.compareTo("") == 0 ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean validPort(final int port) {
        return port >= 0 && port <= 65535;
    }
}
