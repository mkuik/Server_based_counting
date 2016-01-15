package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class ServerListAdapter extends BaseAdapter {

    final String tag = "ServerListAdapter";
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
//        getHostname(address);
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
                    super.onPostExecute(response);
                    Log.i(tag, "hostname response: " + response);
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
    public View getView(final int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater lInflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.server_address_list_item, null);
        }
        final ServerAddress item = (ServerAddress) getItem(position);
        final TextView title = (TextView) view.findViewById(R.id.text1);
        final TextView subtitle = (TextView) view.findViewById(R.id.text2);
        title.setText(item.name);
        subtitle.setText(item.ip + " @ " + item.port);
        if (item.name == null) {
            Global.syncServerName(item, new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
        return view;

    }

    public class SortByIP implements Comparator<ServerAddress> {
        @Override
        public int compare(ServerAddress lhs, ServerAddress rhs) {
            return lhs.ip.compareTo(rhs.ip);
        }
    }
}
