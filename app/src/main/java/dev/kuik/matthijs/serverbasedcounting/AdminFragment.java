package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import java.util.List;

public class AdminFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Global.Adapter {

    ExpandableListView listview;
    UserListAdapter userlistadapter;
    TextView message;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        if (view != null) {
            listview = (ExpandableListView) view.findViewById(R.id.users_listview);
            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            message = (TextView) view.findViewById(R.id.message);
            swipeLayout.setOnRefreshListener(this);
            listview.setAdapter(userlistadapter);
        }
        return view;
    }

    @Override
    public void onRefresh() {
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
        for (User u : users) {
            if (userlistadapter.isNewUser(u.getName())) {
                userlistadapter.addUser(u);
            }
        }
        userlistadapter.notifyDataSetChanged();
    }
}
