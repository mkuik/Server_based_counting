package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class ServerListAdapter extends BaseAdapter {

    static final String tag = "ServerListAdapter";
    ArrayList<ServerAddress> serverAddressItems;
    Context context;

    public ServerListAdapter() {
        serverAddressItems = new ArrayList<>();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void add(final ServerAddress address) {
        for (ServerAddress listItem : serverAddressItems) {
            if (listItem.equals(address)) {
                return;
            }
        }
        serverAddressItems.add(address);
        Collections.sort(serverAddressItems, new SortByIP());
        getHostname(address);
    }

    public void getHostname(final ServerAddress address) {
        JSONObject json = new JSONObject();
        try {
            json.put("hostname", "");
            ServerCommunicator server = new ServerCommunicator(address) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Log.i(tag, "get hostname from " + address.toString());
                }

                @Override
                protected void onPostExecute(String response) {
                    Log.i(tag, "hostname response: " + response);
                    super.onPostExecute(response);
                    if (response == null) return;
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String hostname = jsonResponse.getString("hostname");
                        Log.i(tag, "hostname is " + hostname);
                        address.setName(hostname);
                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.i(tag, e.toString());
                    }
                }
            };
            server.execute(json.toString());
        } catch (JSONException e) {
            Log.i(tag, e.toString());
            return;
        }
    }

    public void clear() {
        serverAddressItems.clear();
    }

    @Override
    public int getCount() {
        return serverAddressItems.size();
    }

    @Override
    public Object getItem(int position) {
        return serverAddressItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null && context != null) {
            LayoutInflater li = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.server_address_list_item, null);
        }
        ServerAddress item = (ServerAddress) getItem(position);
        TextView ip = (TextView) view.findViewById(R.id.ip_address);
        if (ip != null) ip.setText(item.toString());
        return view;

    }

    public class SortByIP implements Comparator<ServerAddress> {
        @Override
        public int compare(ServerAddress lhs, ServerAddress rhs) {
            return lhs.ip.compareTo(rhs.ip);
        }
    }
}
