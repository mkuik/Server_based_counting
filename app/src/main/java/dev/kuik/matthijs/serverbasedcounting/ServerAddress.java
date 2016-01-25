package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Matthijs Kuik on 1-12-2015.
 */

public class ServerAddress {
    public String ip;
    public int port;
    public String name;
    private String status;
    private int color1 = Color.BLACK;
    private int color2 = Color.WHITE;
    private Bitmap icon;


    ServerAddress(final String ip, final int port, final String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    ServerAddress(JSONObject json) throws JSONException {
        this.name = json.getString("name");
        this.ip = json.getString("ip");
        this.port = json.getInt("port");
        this.color1 = json.getInt("color1");
        this.color2 = json.getInt("color2");
        this.status = json.getString("status");
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ip", ip);
        json.put("port", port);
        json.put("name", name);
        json.put("color1", color1);
        json.put("color2", color2);
        json.put("status", status);
        return json;
    }

    public String toString() {
        return ip + ":" + port;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getColor1() {
        return color1;
    }

    public void setColor1(int color1) {
        this.color1 = color1;
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        this.color2 = color2;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getHost() {
        return ip;
    }

    public boolean equals(final ServerAddress address) {
        return port == address.port && ip.compareTo(address.ip) == 0;
    }

    public int getPort() {
        return port;
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
