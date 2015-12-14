package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AdminFragment extends Fragment {

    Adapter listener;
    ExpandableListView listview;
    TextView message;
    Activity activity;
    ServerAddress server;

    public AdminFragment() {
        // Required empty public constructor
    }

    public void getPreferences() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        final String ip = preferences.getString("ip", "");
        final Integer port = preferences.getInt("port", 0);
        if (ip.compareTo("") != 0) {
            server = new ServerAddress(ip, port);
            refreshServerUsers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
    }

    public void refreshServerUsers() {
        if (server != null) {
            ServerCommunicator net = new ServerCommunicator(server) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    message.setText("Requesting users...");
                }

                @Override
                protected void onPostExecute(final String serverResponse) {
                    super.onPostExecute(serverResponse);
                    if (serverResponse == null) {
                        message.setText(String.format("No response from %s", server.getHost()));
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(serverResponse);
                        final JSONArray users = json.getJSONArray("users");
                        for (Integer i = 0; i != users.length(); i++) {
                        }
                        message.setText(String.format("Users %s", users.toString()));
                    } catch (JSONException e) {
                        message.setText(String.format("json error %s (%s)", e.toString(), serverResponse));
                    }
                }
            };
            JSONObject json = new JSONObject();
            try {
                json.put("user", getUsername());
                json.put("function", "users");
            } catch (JSONException e) {
                message.setText(e.toString());
            }
            net.execute(json.toString());
        }
    }

    public String getUsername() {
        AccountManager manager = AccountManager.get(activity);
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

    public static AdminFragment newInstance() {
        AdminFragment fragment = new AdminFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        if (view != null) {
            listview = (ExpandableListView) view.findViewById(R.id.users_listview);
            message = (TextView) view.findViewById(R.id.message);
        }
        return view;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        activity = context;
        if (context instanceof Adapter) {
            listener = (Adapter) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Adapter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Adapter {
    }
}
