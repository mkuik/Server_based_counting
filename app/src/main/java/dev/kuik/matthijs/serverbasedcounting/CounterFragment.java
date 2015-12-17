package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Adapter} interface
 * to handle interaction events.
 * Use the {@link CounterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CounterFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Adapter listener;
    private Activity activity;
    private TextView message;
    private TextView counter;
    private TextView subtotal;
    private Integer submit_number = 0;
    private Integer max_server_number = 0;
    private Integer server_number = 0;
    private Button submitButton;
    private Button incrementButton;
    private Button decrementButton;
    private ServerAddress server;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private static SwipeRefreshLayout swipeLayout;
    JSONObject json;


    public CounterFragment() {
        // Required empty public constructor
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.compareTo("ip") == 0 || key.compareTo("port") == 0) {
                    Log.i("IP CHANGED", key);
                    getPreferences();
                    refreshServerStatus();
                }
            }
        };
    }

    public void getPreferences() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        final Integer counter = preferences.getInt("count", 0);
        final Integer subtotal = preferences.getInt("subtotal", 0);
        final String ip = preferences.getString("ip", "");
        final Integer port = preferences.getInt("port", 0);
        final String hostname = preferences.getString("hostname", "");
        final Integer max = preferences.getInt("max", 0);
        this.counter.setText(counter.toString());
        this.subtotal.setText(subtotal.toString());
        if (ip.compareTo("") != 0) {
            server = new ServerAddress(ip, port, hostname);
        }
        this.max_server_number = max;
    }

    public void refreshSubtotal() {
        subtotal.setText(submit_number.toString());
    }

    public void increment() {
        submit_number++;
        refreshSubtotal();
        checkCounterRange();
    }

    public void decrement() {
        submit_number--;
        refreshSubtotal();
        checkCounterRange();
    }

    public void submit() {
        json = new JSONObject();
        try {
            json.put("subtotal", submit_number);
            refreshServerStatus();
            submit_number = 0;
            refreshSubtotal();
            checkCounterRange();
        } catch (JSONException e) {
            message.setText(e.toString());
        }
    }

    public void checkCounterRange() {
        final int sum = server_number + submit_number;
        decrementButton.setEnabled(sum > 0);
        incrementButton.setEnabled(sum < Integer.MAX_VALUE);
        submitButton.setEnabled(submit_number != 0);
        if (sum > max_server_number && max_server_number != 0) {
            message.setBackgroundColor(activity.getResources().getColor(R.color.server_error));
            message.setText("Counter limit reached");
        } else {
            message.setBackgroundColor(Color.TRANSPARENT);
            message.setText("");
        }
    }

    public void refreshServerStatus() {
        if (json == null) json = new JSONObject();
        if (server != null) {
            swipeLayout.setRefreshing(true);
            ServerCommunicator net = new ServerCommunicator(server) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    message.setText("Connecting to server");
                }

                @Override
                protected void onPostExecute(final String serverResponse) {
                    super.onPostExecute(serverResponse);
                    swipeLayout.setRefreshing(false);
                    if (serverResponse == null) {
                        if (listener != null) listener.onServerDisconnected(server);
                        message.setText(String.format("No response from %s", server.toString()));
                        return;
                    }
                    try {
                        JSONObject jsonResonse = new JSONObject(serverResponse);
                        if (listener != null) listener.onServerResponse(server, jsonResonse);
                        server.setName(jsonResonse.getString("hostname"));
                        server_number = jsonResonse.getInt("count");
                        max_server_number = jsonResonse.getInt("max");
                        message.setText(String.format("Connected to %s", server.toString()));
                        counter.setText(server_number.toString());
                        checkCounterRange();
                    } catch (JSONException e) {
                        message.setText(String.format("json error %s (%s)", e.toString(), serverResponse));
                    }
                }
            };
            try {
                json.put("user", getUsername());
                json.put("function", "status");
            } catch (JSONException e) {
                message.setText(e.toString());
            }
            net.execute(json.toString());
        }
        json = null;
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

    public void setPreferences() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (server != null) {
            editor.putString("ip", server.ip);
            editor.putInt("port", server.port);
            editor.putString("hostname", server.name);
        }
        editor.putInt("count", Integer.parseInt(counter.getText().toString()));
        editor.putInt("subtotal", Integer.parseInt(subtotal.getText().toString()));
        editor.putInt("max", max_server_number);
        editor.commit();
    }

    public static CounterFragment newInstance() {
        CounterFragment fragment = new CounterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
        onRefresh();
        checkCounterRange();
    }

    @Override
    public void onStop() {
        super.onStop();
        setPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_counter, container, false);
        counter = (TextView) view.findViewById(R.id.counter);
        subtotal = (TextView) view.findViewById(R.id.subtotal);
        incrementButton = (Button) view.findViewById(R.id.increment_button);
        decrementButton = (Button) view.findViewById(R.id.decrement_button);
        submitButton = (Button) view.findViewById(R.id.submit_button);
        message = (TextView) view.findViewById(R.id.message);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        activity = context;
        if (context instanceof Adapter) {
            listener = (CounterFragment.Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Adapter");
        }

        activity.getPreferences(Context.MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onRefresh() {
        refreshServerStatus();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Adapter extends ServerStatusAdapter {
    }
}
