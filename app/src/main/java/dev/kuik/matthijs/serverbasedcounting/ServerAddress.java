package dev.kuik.matthijs.serverbasedcounting;

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
    public String ip = "";
    public InetAddress net;
    public Integer port = 0;
    public String name = "";

    ServerAddress(final String ip, final int port, final String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String toString() {
        if (name.compareTo("") == 0)
            return ip + ":" + port.toString();
        else
            return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getHost() {
        if (net != null) {
            if (name.compareTo(ip) == 0) {
                return ip;
            } else {
                return name + " @ " + ip;
            }
        } else {
            return ip;
        }
    }


    public boolean equals(final ServerAddress address) {
        return ip.compareTo(address.ip) == 0 && port == address.port;
    }

    public String getPort() {
        if (port != null) {
            return port.toString();
        } else {
            return "NA";
        }
    }
}
