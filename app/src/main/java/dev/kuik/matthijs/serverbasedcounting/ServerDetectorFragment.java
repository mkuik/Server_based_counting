package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ServerDetectorFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private final String tag = "SelectServerFragment";
    public final ServerListAdapter servers = new ServerListAdapter();
    private SwipeRefreshLayout swipeLayout;
    private ServerDetector detector;

    public ServerDetectorFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (servers.isEmpty()) onRefresh();
    }

    public void setRefreshing(final boolean refreshing) {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(refreshing);
            }
        });
    }

    public void scanNetwork() {
        if (detector != null) detector.cancel(true);
        detector = new ServerDetector(getActivity()) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setRefreshing(true);
                Log.i(tag, "start detector " + this.toString());
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                setRefreshing(false);
                detector = null;
                Log.i(tag, "cancel detector " + this.toString());
            }
            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                setRefreshing(false);
                detector = null;
                Log.i(tag, "stop detector " + this.toString());
            }
            @Override
            protected void onProgressUpdate(ServerAddress... values) {
                super.onProgressUpdate(values);
                newPort(values[0]);
            }
        };
        detector.execute();
    }

    public void newPort(final ServerAddress address) {
        servers.add(address);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        if (view != null) {
            ListView serversListView = (ListView) view.findViewById(R.id.serverList);
            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(this);
            serversListView.setAdapter(servers);

            serversListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Global.setHost((ServerAddress) servers.getItem(position));
                    Global.setUser(new User(getActivity()));
                    Global.notifyHost();
                    Global.notifyTheme();
                }
            });
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        scanNetwork();
    }

    class ServerListAdapter extends BaseAdapter {

        final String tag = "ServerListAdapter";
        final ArrayList<ServerAddress> serverAddressItems = new ArrayList<>();
        ListItemWithIcon container;

        public ServerListAdapter() {}

        public void add(final ServerAddress address) {
            synchronized (serverAddressItems) {
                for (int i = 0; i != serverAddressItems.size(); ++i) {
                    final ServerAddress item = serverAddressItems.get(i);
                    if (item.port == address.port && item.ip.compareTo(address.ip) == 0) {
                        serverAddressItems.remove(i);
                        serverAddressItems.add(i, address);
                        notifyDataSetChanged();
                        Log.i(tag, "update server " + address.toString());
                        return;
                    }
                }
                Log.i(tag, "new server " + address.toString());
                serverAddressItems.add(address);
                Collections.sort(serverAddressItems, new SortByIP());
            }
            notifyDataSetChanged();
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
            final ServerAddress item = serverAddressItems.get(position);
            container = new ListItemWithIcon(getActivity());
            container.setTitle(item.name);
            container.setSubtitle(item.ip + " : " + item.port);
            container.setIconBackground(item.getColor1());
            container.setIcon(item.getIcon());
            return container;

        }

        public class SortByIP implements Comparator<ServerAddress> {
            @Override
            public int compare(ServerAddress lhs, ServerAddress rhs) {
                return lhs.ip.compareTo(rhs.ip);
            }
        }
    }
}
