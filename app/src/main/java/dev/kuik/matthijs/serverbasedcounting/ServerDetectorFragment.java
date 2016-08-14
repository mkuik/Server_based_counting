package dev.kuik.matthijs.serverbasedcounting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ServerDetectorFragment extends Fragment implements AdapterView.OnItemClickListener{

    private final String tag = "SelectServerFragment";
    private CouterDetector detector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_server, container, false);
        if (view != null) {
            ListView serversListView = (ListView) view.findViewById(R.id.serverList);
            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            ProgressBar bar = (ProgressBar) view.findViewById(R.id.progressBar);
            detector = new CouterDetector(getActivity(), R.layout.listitem_2_lines_with_icon, swipeLayout, bar);
            serversListView.setAdapter(detector);
            serversListView.setOnItemClickListener(this);

            detector.scan();
        }
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Pair<User, Counter> item = detector.getItem(position);
        if (item.second.getPassword().equals("")) {
            Global.setHost(item.second);
            Global.setUser(item.first);
            Global.notifyHost();
            Global.notifyTheme();
        } else {
            final View dialog = getActivity().getLayoutInflater().inflate(R.layout.password_dialog, null);
            final EditText inputField = (EditText) dialog.findViewById(R.id.password_dialog_field);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialog);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (item.second.getPassword().equals(inputField.getText().toString())) {
                        Global.setHost(item.second);
                        Global.setUser(item.first);
                        Global.notifyHost();
                        Global.notifyTheme();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            // Create the AlertDialog object and return it
            builder.create();
            builder.show();
        }

    }
}
