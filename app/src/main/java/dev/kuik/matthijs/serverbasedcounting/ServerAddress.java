package dev.kuik.matthijs.serverbasedcounting;

import android.graphics.Bitmap;
import android.graphics.Color;
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
    private int color1 = Color.BLACK;
    private int color2 = Color.WHITE;
    private Bitmap icon;


    ServerAddress(final String ip, final int port, final String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    ServerAddress(JSONObject json) throws JSONException {
        this.name = json.getString("name");
        this.ip = json.getString("ip");
        this.port = json.getInt("port");
        this.color1 = json.getInt("color1");
        this.color2 = json.getInt("color2");
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ip", ip);
        json.put("port", port);
        json.put("name", name);
        json.put("color1", color1);
        json.put("color2", color2);
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
}
