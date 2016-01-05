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
        implements LocalNetworkServerDetector.Adapter,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String tag = "SelectServerFragment";
    private static ListView serversListView;
    private static TextView message;
    public static final ServerListAdapter servers = new ServerListAdapter();
    private static LocalNetworkServerDetector scanner;
    private static Handler handler;
    private Adapter listener;
    private static SwipeRefreshLayout swipeLayout;

    private static Context context;

    public SelectServerFragment() {
        handler = new Handler(Looper.getMainLooper());
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
        final WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiMgr.getConnectionInfo().getIpAddress();
    }

    @Override
    public void onResume() {
        super.onResume();
        View view;
        if ((view = getView()) != null) {
            swipeLayout.measure(view.getWidth(), view.getHeight());
        }
        onRefresh();
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
                swipeLayout.setRefreshing(false);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        serversListView = (ListView) view.findViewById(R.id.serverList);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        message = (TextView) view.findViewById(R.id.message);
        swipeLayout.setOnRefreshListener(this);
        serversListView.setAdapter(servers);

        serversListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, "click " + position + " " +  servers.getItem(position), Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onSelectedAddress((ServerAddress) servers.getItem(position));
                }
            }
        });

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
        if (scanner != null) scanner.removeListener();
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
