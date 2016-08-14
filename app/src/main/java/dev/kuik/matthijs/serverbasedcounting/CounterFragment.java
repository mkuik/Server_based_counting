package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CounterFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Global.Adapter {

    private TextView message;
    private TextView counter;
    private TextView subtotal;
    private ThemeButton submitButton;
    private ThemeButton incrementButton;
    private ThemeButton decrementButton;
    private SwipeRefreshLayout swipeLayout;
    private Integer submit_value = 0;

    public CounterFragment() {
        // Required empty public constructor
    }

    public void setTheme() {
//        Toast.makeText(getActivity(), "Set theme", Toast.LENGTH_SHORT).show();
        if (submitButton != null) submitButton.setColor(Global.getColor1());
        if (incrementButton != null) incrementButton.setColor(Global.getColor1());
        if (decrementButton != null) decrementButton.setColor(Global.getColor1());
    }

    public void increment() {
        submit_value++;
        setCounterVariables();
        checkCounterRange();
    }

    public void decrement() {
        submit_value--;
        setCounterVariables();
        checkCounterRange();
    }

    public void submit() {
        new Com.Edit(submit_value) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                submit_value = 0;
                setCounterVariables();
            }

            @Override
            protected void onStatus(int count, int max) {
                super.onStatus(count, max);
                Global.getHost().setCounter(count);
                Global.getHost().setMax(max);
                setCounterVariables();
                checkCounterRange();
            }
        }.execute();
    }

    public void setCounterVariables() {
        if (counter != null) counter.setText(Global.getCounterValue().toString());
        if (subtotal != null) subtotal.setText(
                (submit_value < 0 ? "" : "+") + submit_value.toString());
    }

    public void checkCounterRange() {
        final int sum = Global.getCounterValue() + submit_value;
        if (decrementButton != null) decrementButton.setEnabled(sum > 0);
        if (incrementButton != null) incrementButton.setEnabled(sum < Integer.MAX_VALUE);
        if (submitButton != null) submitButton.setEnabled(submit_value != 0);
        if (sum > Global.getCounterMaxValue() && Global.getCounterMaxValue() != 0) {
            message.setText("Limit reached");
        } else {
            message.setText("");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.addListener(this);
        onRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCounterRange();
        setTheme();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_counter, container, false);
        if (view != null) {
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

            counter.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Global.overrideCounter(getActivity());
                    return true;
                }
            });

            setCounterVariables();
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Global.removeListener(this);
    }

    @Override
    public void onRefresh() {
//        setRefreshing(true);
    }

    public void setRefreshing(final boolean refreshing) {
        if (swipeLayout != null) {
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(refreshing);
                }
            });
        }
    }

    @Override
    public void OnHostAddressChanged(Counter address) {

    }

    @Override
    public void OnThemeChanged(Bitmap icon, int color1, int color2) {
        setTheme();
    }

    @Override
    public void OnCounterValueChanged(int counter, int max) {
        setRefreshing(false);
        setCounterVariables();
        checkCounterRange();
    }

    @Override
    public void OnUserChanged(User user) {

    }
}
