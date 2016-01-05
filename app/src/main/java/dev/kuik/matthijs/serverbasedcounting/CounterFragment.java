package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
public class CounterFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Global.Adapter {

    private Adapter listener;
    private Activity activity;
    private TextView message;
    private TextView counter;
    private TextView subtotal;
    private ThemeButton submitButton;
    private ThemeButton incrementButton;
    private ThemeButton decrementButton;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private static SwipeRefreshLayout swipeLayout;
    JSONObject json;

    public CounterFragment() {
        // Required empty public constructor
    }

    public void setTheme() {
        Toast.makeText(getActivity(), "Set theme", Toast.LENGTH_SHORT).show();
        if (submitButton != null) submitButton.setColor(Global.getColor1());
        if (incrementButton != null) incrementButton.setColor(Global.getColor1());
        if (decrementButton != null) decrementButton.setColor(Global.getColor1());
    }

    public void increment() {
        Global.submit_value++;
        setCounterSubtotal();
        checkCounterRange();
    }

    public void decrement() {
        Global.submit_value--;
        setCounterSubtotal();
        checkCounterRange();
    }

    public void submit() {
        Global.submit_buffer_value += Global.submit_value;
        Global.counter_value += Global.submit_value;
        Global.submit_value = 0;
        setCounterTotal();
        setCounterSubtotal();
        checkCounterRange();
        refreshServerStatus();
    }

    public void setCounterTotal() {
        if (counter != null) counter.setText(Global.counter_value.toString());
    }

    public void setCounterSubtotal() {
        if (subtotal != null) subtotal.setText(
                (Global.submit_value < 0 ? "" : "+") + Global.submit_value.toString());
    }

    public void checkCounterRange() {
        final int sum = Global.counter_value + Global.submit_value;
        if (decrementButton != null) decrementButton.setEnabled(sum > 0);
        if (incrementButton != null) incrementButton.setEnabled(sum < Integer.MAX_VALUE);
        if (submitButton != null) submitButton.setEnabled(Global.submit_value != 0);
        if (sum > Global.counter_max_value && Global.counter_max_value != 0) {
            message.setBackgroundColor(activity.getResources().getColor(R.color.server_error));
            message.setText("Counter limit reached");
        } else {
            message.setBackgroundColor(Color.TRANSPARENT);
            message.setText("");
        }
    }

    public void refreshServerStatus() {
        if (json == null) json = new JSONObject();
        if (Global.getHost() != null) {
            swipeLayout.setRefreshing(true);
            ServerCommunicator net = new ServerCommunicator(Global.getHost()) {
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
                        if (listener != null) listener.onServerDisconnected(Global.getHost());
                        Global.setIsInSyncWithHost(false);
                        message.setText(String.format("No response from %s", Global.getHost().toString()));
                        return;
                    }
                    Global.setHostResponse(serverResponse);
                    try {
                        JSONObject jsonResonse = new JSONObject(serverResponse);
                        if (listener != null) listener.onServerResponse(Global.getHost(), jsonResonse);
                        Global.setIsInSyncWithHost(true);
                        Global.getHost().setName(jsonResonse.getString("hostname"));
                        Global.setCounterValue(jsonResonse.getInt("count"));
                        Global.setCounterMaxValue(jsonResonse.getInt("max"));
                        Global.setSubmitBufferValue(0);
                        setCounterTotal();
                        checkCounterRange();
                    } catch (JSONException e) {
                        message.setText(String.format("json error %s (%s)", e.toString(), serverResponse));
                    }
                }
            };
            try {
                json.put("user", getUsername());
                json.put("function", "status");
                json.put("subtotal", Global.submit_buffer_value);
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
        Global.addListener(this);
        onRefresh();
        checkCounterRange();
        setTheme();
    }

    @Override
    public void onStop() {
        super.onStop();
        Global.removeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_counter, container, false);
        counter = (TextView) view.findViewById(R.id.counter);
        subtotal = (TextView) view.findViewById(R.id.subtotal);
        incrementButton = (ThemeButton) view.findViewById(R.id.increment_button);
        decrementButton = (ThemeButton) view.findViewById(R.id.decrement_button);
        submitButton = (ThemeButton) view.findViewById(R.id.submit_button);
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

        setCounterTotal();
        setCounterSubtotal();
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

    @Override
    public void OnHostAddressChanged(ServerAddress address) {
        onRefresh();
    }

    @Override
    public void OnHostStatusChanged(ServerAddress address, Boolean connected) {

    }

    @Override
    public void OnHostResponseRecieved(ServerAddress address, String response) {

    }

    @Override
    public void OnThemeChanged(int color1, int color2) {
        setTheme();
    }

    @Override
    public void OnCounterValueChanged(int counter, int submit, int buffer, int max) {

    }

    public interface Adapter extends ServerStatusAdapter {
    }
}
