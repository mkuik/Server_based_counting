package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class DefineServerActivity extends ActionBarActivity
        implements SelectServerFragment.OnSelectedSocket {

    private static final String tag = "DefineServerActivity";
    private static SelectServerFragment serverSelect;
    private static Thread thread;
    private static ServerAddress server;
    private static Socket socket;
    private static Handler handler;
    private static final int timeout = 1000;

    public DefineServerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_server);
//        serverResponse = (TextView) findViewById(R.id.server_response);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    serverSelect.scanNetwork();
                }
            });
        }
        return view;
    }

    @Override
    public void onSelectedAddress(ServerAddress address) {
        Log.i(tag, address.getHost());
        server = address;
        ServerCommunicator net = new ServerCommunicator(address) {
            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    Log.i(tag, s);
                    Toast t = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        };
        try {
            JSONObject json = new JSONObject();
            json.put("subtotal", 5);
            json.put("cient", getUsername());
            net.execute(json.toString());
        } catch (JSONException e) {
            Log.e(tag, e.toString());
        }
    }

    @Override
    public void onCreateServerAddress(ServerAddress address) {
        onSelectedAddress(address);
    }

    public String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return null;
    }
}
