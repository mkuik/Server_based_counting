package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SelectServerFragment extends Fragment implements LocalNetworkServerDetector.Adapter {

    private static TextView hostConnection;
    private static TextView hostPortConnection;
    private static TextView message;

    private static ProgressBar progressBar;
    private static ListView serversListView;
    private static ServerListAdapter servers;
    private static Button startScanningNetwork;
    private static LocalNetworkServerDetector scanner;
    private static Handler handler;

    private static Context context;

    public SelectServerFragment() {
        scanner = new LocalNetworkServerDetector(this);
        handler = new Handler(Looper.getMainLooper());
        servers = new ServerListAdapter();
    }

    public void scanNetwork() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final WifiManager wifiMgr = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                final int ip = wifiMgr.getConnectionInfo().getIpAddress();
                final String ipBase = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
                final String ipFull = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
                scanner.setIPDomain(ipBase);
                Thread scannerThread = new Thread(scanner);
                scannerThread.start();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        message.setText(ipFull);
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void foundOpenPort(final String ip, final int port) {
        servers.addServerAddress(ip, port);
        handler.post(new Runnable() {
            public void run() {
                servers.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void OnProgressUpdate(final double percentage) {
        final int progress = (int) (percentage * progressBar.getMax());
        handler.post(new Runnable() {
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    @Override
    public void OnIPError(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hostConnection.setText(message);
            }
        });
    }

    @Override
    public void OnPortError(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hostPortConnection.setText(message);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        serversListView = (ListView) view.findViewById(R.id.serverList);
        startScanningNetwork = (Button) view.findViewById(R.id.startScanButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        hostConnection = (TextView) view.findViewById(R.id.status1);
        hostPortConnection = (TextView) view.findViewById(R.id.status2);
        message = (TextView) view.findViewById(R.id.status3);

        serversListView.setAdapter(servers);

        startScanningNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNetwork();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        servers.setContext(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanner.abort();
    }

}
