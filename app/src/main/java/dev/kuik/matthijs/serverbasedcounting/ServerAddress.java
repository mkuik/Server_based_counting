package dev.kuik.matthijs.serverbasedcounting;

import android.graphics.Bitmap;
import android.graphics.Color;

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
