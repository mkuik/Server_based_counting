package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
    private OnClickListener adapter;

    public ServerListAdapter() {
        serverAddressItems = new ArrayList<>();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public interface OnClickListener {
        void onClick(int position);
    }

    public void setOnClickListener(OnClickListener adapter) {
        this.adapter = adapter;
    }

    public void add(final ServerAddress address) {
        for (ServerAddress listItem : serverAddressItems) {
            if (listItem.equals(address)) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ServerAddressViewHolder viewHolder;

        if (convertView == null && context != null) {
            LayoutInflater li = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.server_address_list_item, null);
            viewHolder = new ServerAddressViewHolder(view);
            view.setTag(viewHolder);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null) adapter.onClick(position);
                }
            });
        } else {
            viewHolder = (ServerAddressViewHolder) view.getTag();
        }

        ServerAddress item = (ServerAddress) getItem(position);
        viewHolder.ip.setText(item.getHost());
        viewHolder.port.setText(item.getPort());

        return view;

    }

    public class SortByIP implements Comparator<ServerAddress> {
        @Override
        public int compare(ServerAddress lhs, ServerAddress rhs) {
            return lhs.ip.compareTo(rhs.ip);
        }
    }

    public class ServerAddressViewHolder {
        public TextView ip;
        public TextView port;

        ServerAddressViewHolder(View view) {
            ip = (TextView) view.findViewById(R.id.ip_address);
            port = (TextView) view.findViewById(R.id.port_number);
        }
    }
}
