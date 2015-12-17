package dev.kuik.matthijs.serverbasedcounting;

import org.json.JSONObject;

/**
 * Created by Matthijs Kuik on 16-12-2015.
 */
public interface ServerStatusAdapter {
    void onServerDisconnected(ServerAddress address);
    void onServerResponse(ServerAddress address, JSONObject response);
}
