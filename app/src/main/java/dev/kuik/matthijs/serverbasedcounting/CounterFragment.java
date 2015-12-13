package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Adapter} interface
 * to handle interaction events.
 * Use the {@link CounterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CounterFragment extends Fragment {

    private Adapter mListener;
    private Activity activity;
    private TextView message;
    private TextView counter;
    private TextView subtotal;
    private Button submit;
    private Button increment;
    private Button decrement;
    private ServerAddress server;

    public CounterFragment() {
        // Required empty public constructor
    }

    public void getPreferences() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        final Integer counter = preferences.getInt("count", 0);
        final Integer subtotal = preferences.getInt("subtotal", 0);
        final String ip = preferences.getString("ip", "");
        final Integer port = preferences.getInt("port", 0);
        this.counter.setText(counter.toString());
        this.subtotal.setText(subtotal.toString());
        if (ip.compareTo("") != 0) {
          server = new ServerAddress(ip, port);
        }
    }

    public void setPreferences() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (server != null) {
            editor.putString("ip", server.ip);
            editor.putInt("port", server.port);
        }
        editor.putInt("count", Integer.parseInt(counter.getText().toString()));
        editor.putInt("subtotal", Integer.parseInt(subtotal.getText().toString()));
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
        increment = (Button) view.findViewById(R.id.increment_button);
        decrement = (Button) view.findViewById(R.id.decrement_button);
        submit = (Button) view.findViewById(R.id.submit_button);
        message = (TextView) view.findViewById(R.id.message);
        return view;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        activity = context;
        if (context instanceof Adapter) {
            mListener = (CounterFragment.Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Adapter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface Adapter {

    }
}
