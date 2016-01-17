package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        Global.Adapter, UserListAdapter.OnUserOptionListener {

    ExpandableListView listview;
    UserListAdapter userlistadapter;
    SwipeRefreshLayout swipeLayout;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.addListener(this);
        onRefresh();
    }

    public void setRefreshing(final boolean refreshing) {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(refreshing);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Global.removeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userlistadapter = new UserListAdapter();
        userlistadapter.setInflater((LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        userlistadapter.addOptionClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        if (view != null) {
            listview = (ExpandableListView) view.findViewById(R.id.users_listview);
            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(this);
            listview.setAdapter(userlistadapter);
            listview.setClickable(true);
        }
        return view;
    }

    @Override
    public void onRefresh() {
        setRefreshing(true);
        Global.syncUsers();
    }

    @Override
    public void OnHostAddressChanged(ServerAddress address) {

    }

    @Override
    public void OnHostResponseRecieved(ServerAddress address, String response) {

    }

    @Override
    public void OnHostResponseLost(ServerAddress address, String response) {

    }

    @Override
    public void OnThemeChanged(Bitmap icon, int color1, int color2) {

    }

    @Override
    public void OnCounterValueChanged(int counter, int max) {

    }

    @Override
    public void OnUserListRecieved(List<User> users) {
        setRefreshing(false);
        for (User u : users) userlistadapter.addUser(u);
        userlistadapter.notifyDataSetChanged();
    }

    @Override
    public void OnOptionClick(User user) {
        Global.syncUserRights(user);
        try {
            Log.i("click", user.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
