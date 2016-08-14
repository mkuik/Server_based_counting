package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Matthijs Kuik on 23-11-2015.
 */
public class CouterDetector extends ArrayAdapter<Pair<User, Counter>> implements SwipeRefreshLayout.OnRefreshListener {

    String baseIP;
    final String tag = "ServerDetector";
    final int[] ip_range = {0, 255};
    final int port = 4500;
    String external;
    final Object LOCK = new Object();
    SwipeRefreshLayout layout;
    ProgressBar bar;
    final int task_count = ip_range[1] - ip_range[0] + 1;
    int task_completed;
    int resource;

    public CouterDetector(Context context, int resource, SwipeRefreshLayout layout, ProgressBar bar) {
        super(context, resource);
        this.resource = resource;
        this.layout = layout;
        this.bar = bar;
        bar.setMax(task_count);
        bar.setVisibility(View.GONE);
        init();
    }

    public void init() {
        final int ip = Counter.getIP(getContext());
        baseIP = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
        external = getContext().getString(R.string.teller_openweek_ip);
        layout.setOnRefreshListener(this);
    }

    public void setRefreshing(final boolean refreshing) {
        if (layout != null) {
            layout.post(new Runnable() {
                @Override
                public void run() {
                    layout.setRefreshing(refreshing);
                }
            });
        }
    }

    private void incrementCompletedTasks() {
        task_completed++;
        if (task_completed == task_count) {
            setRefreshing(false);
            bar.setVisibility(View.GONE);
        }
        if (bar != null) bar.setProgress(task_completed);
    }

    public void scan() {
        clear();
        setRefreshing(true);
        test(external, port);
        for (int ipAddress = ip_range[0]; ipAddress < ip_range[1]; ipAddress++) {
            final String ip = baseIP + Integer.toString(ipAddress);
            test(ip, port);
        }
    }

    private void test(String ip, int port) {
        task_completed = 0;
        bar.setVisibility(View.VISIBLE);
        new Com.Meta(ip, port) {
            @Override
            protected JSONObject doInBackground(JSONObject... params) {
                JSONObject json;
                synchronized (LOCK) {
                    json = super.doInBackground(params);
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                incrementCompletedTasks();
            }

            @Override
            protected void onCounter(User account, Counter counter) {
                super.onCounter(account, counter);
                add(new Pair<User, Counter>(account, counter));
                notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onRefresh() {
        scan();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.text1 = (TextView) row.findViewById(R.id.text1);
            holder.text2 = (TextView) row.findViewById(R.id.text2);
            holder.image = (IconView) row.findViewById(R.id.icon);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final Pair<User, Counter> item = getItem(position);
        holder.text1.setText(item.second.getAddress().toString());
        holder.text2.setText(item.first.getName());
        holder.image.setColor(Color.parseColor(item.second.getColor1()));
        holder.image.setImageBitmap(item.second.getIconBitmap());
        Log.d("Detector", "Get view");
        return row;
    }

    private class ViewHolder {
        public TextView text1;
        public TextView text2;
        public IconView image;
    }
}