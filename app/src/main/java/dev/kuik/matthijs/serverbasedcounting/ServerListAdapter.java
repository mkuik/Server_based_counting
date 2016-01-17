package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
        for (int i = 0; i!= serverAddressItems.size(); ++i) {
            final ServerAddress item = serverAddressItems.get(i);
            if (item.port == address.port && item.ip.compareTo(address.ip) == 0) {
                Log.i(tag, address.toString() + " equals " + item.toString());
                serverAddressItems.remove(i);
                serverAddressItems.add(i, address);
                return;
            }
        }
        serverAddressItems.add(address);
        Collections.sort(serverAddressItems, new SortByIP());
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
            view = lInflater.inflate(R.layout.listitem_2_lines_with_icon, null);
        }
        final ServerAddress item = (ServerAddress) getItem(position);
        final TextView title = (TextView) view.findViewById(R.id.text1);
        final TextView subtitle = (TextView) view.findViewById(R.id.text2);
        final ServerIconView icon = (ServerIconView) view.findViewById(R.id.icon);
        title.setText(item.name);
        subtitle.setText(item.ip + " : " + item.port);
        icon.setColor(item.getColor1());
        icon.setImageBitmap(item.getIcon());
        return view;

    }

    public class SortByIP implements Comparator<ServerAddress> {
        @Override
        public int compare(ServerAddress lhs, ServerAddress rhs) {
            return lhs.ip.compareTo(rhs.ip);
        }
    }
}
