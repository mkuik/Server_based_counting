package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectServerFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String tag = "SelectServerFragment";
    private static ListView serversListView;
    private static TextView message;
    public static final ServerListAdapter servers = new ServerListAdapter();
    private static SwipeRefreshLayout swipeLayout;

    public SelectServerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        servers.setContext(getActivity());

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public int getIP() {
        final WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        return wifiMgr.getConnectionInfo().getIpAddress();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void scanNetwork() {
        swipeLayout.setRefreshing(true);
        servers.clear();
        servers.notifyDataSetChanged();
        final int ip = getIP();
        final String ipBase = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
        LocalNetworkServerDetector scanner = new LocalNetworkServerDetector(ipBase) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.i(tag, "scan network");
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                swipeLayout.setRefreshing(false);
                Log.i(tag, "scan cancelled");
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                swipeLayout.setRefreshing(false);
                Log.i(tag, "scan finished");
            }

            @Override
            protected void onProgressUpdate(ServerAddress... values) {
                super.onProgressUpdate(values);
                final ServerAddress address = values[0];
                if (address != null) {
                    message.setText(address.toString());
                    newPort(address);
                }
                Log.i(tag, "scan update");
            }
        };
        scanner.execute("");
    }

    public void newPort(final ServerAddress address) {
        servers.add(address);
        servers.notifyDataSetChanged();
        Log.i(address.getHost(), "new address " + address.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        if (view != null) {
            serversListView = (ListView) view.findViewById(R.id.serverList);
            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            message = (TextView) view.findViewById(R.id.message);
            swipeLayout.setOnRefreshListener(this);
            serversListView.setAdapter(servers);

            serversListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Global.setHost((ServerAddress) servers.getItem(position));
                    Global.setUser(getActivity());
                    Global.notifyHost();
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
}
