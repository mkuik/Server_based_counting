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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SelectServerFragment extends Fragment
        implements LocalNetworkServerDetector.Adapter, ServerListAdapter.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String tag = "SelectServerFragment";
    private static ListView serversListView;
    private static TextView message;
    public static final ServerListAdapter servers = new ServerListAdapter();
    private static Button startScanningNetwork;
    private LocalNetworkServerDetector scanner;
    private static Handler handler;
    private Adapter listener;
    private static SwipeRefreshLayout swipeLayout;

    private static Context context;

    public SelectServerFragment() {
        handler = new Handler(Looper.getMainLooper());
        servers.setOnClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public int getIP() {
        final WifiManager wifiMgr = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        return wifiMgr.getConnectionInfo().getIpAddress();
    }

    @Override
    public void onResume() {
        super.onResume();
        View view;
        if ((view = getView()) != null) {
            swipeLayout.measure(view.getWidth(), view.getHeight());
        }
    }

    public void scanNetwork() {
        if (scanner != null) {
            scanner.cancel(true);
        }
        swipeLayout.setRefreshing(true);
        servers.clear();
        servers.notifyDataSetChanged();
        final int ip = getIP();
        final String ipBase = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
        scanner = new LocalNetworkServerDetector(this, ipBase) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.i(tag, "scan network");
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                scanner = null;
                Log.i(tag, "scan cancelled");
            }

            @Override
            protected void onPostExecute(String s) {
                Log.i(tag, "scan finished");
                super.onPostExecute(s);
                swipeLayout.setRefreshing(false);
                message.setText("");
            }

            @Override
            protected void onProgressUpdate(String... values) {
                //Log.i(tag, values[0]);
                super.onProgressUpdate(values);
                message.setText(values[0]);
            }
        };
        scanner.execute("");
    }

    @Override
    public void foundOpenPort(final ServerAddress address) {
        servers.add(address);
        handler.post(new Runnable() {
            public void run() {
                servers.notifyDataSetChanged();
            }
        });
        listener.onCreateServerAddress(address);
        Log.i(address.getHost(), "connected to port " + address.getPort());
    }

    @Override
    public void OnStatusReport(final String message) {
        //Log.i("socket test", message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        serversListView = (ListView) view.findViewById(R.id.serverList);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        message = (TextView) view.findViewById(R.id.message);
        swipeLayout.setOnRefreshListener(this);
        serversListView.setAdapter(servers);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        servers.setContext(activity);
        try {
            listener = (Adapter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Adapter");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(int position) {
        if (listener != null) {
            listener.onSelectedAddress((ServerAddress) servers.getItem(position));
        }
    }

    @Override
    public void onRefresh() {
        scanNetwork();
    }

    public interface Adapter {
        void onSelectedAddress(ServerAddress address);

        void onCreateServerAddress(ServerAddress address);
    }
}
