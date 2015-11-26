package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class ServerListAdapter extends BaseAdapter {

    ArrayList<ServerAddress> serverAddressItems;
    Context context;

    public ServerListAdapter() {
        serverAddressItems = new ArrayList<>();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void addServerAddress(final String ip, final int port) {
        for (ServerAddress address : serverAddressItems) {
            if (address.ip.compareTo(ip) == 0 && address.port == port) {
                return;
            }
        }
        serverAddressItems.add(new ServerAddress(ip, port));
        Collections.sort(serverAddressItems, new SortByIP());
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ServerAddressViewHolder viewHolder;

        if (convertView == null && context != null) {
            LayoutInflater li = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.server_address_list_item, null);
            viewHolder = new ServerAddressViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ServerAddressViewHolder) view.getTag();
        }

        ServerAddress item = (ServerAddress) getItem(position);
        viewHolder.ip.setText(item.ip);
        viewHolder.port.setText(Integer.toString(item.port));

        return view;

    }

    public class ServerAddress {
        public String ip;
        public int port;

        ServerAddress(final String ip, final int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    public class SortByIP implements Comparator<ServerAddress> {

        @Override
        public int compare(ServerAddress lhs, ServerAddress rhs) {
            final int ip = lhs.ip.compareTo(rhs.ip);
            if (ip == 0) {
                return lhs.port - rhs.port;
            } else {
                return ip;
            }
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
