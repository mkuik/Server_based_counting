package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UserRightsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        Global.Adapter {

    ExpandableListView listview;
    UserListAdapter userlistadapter;
    SwipeRefreshLayout swipeLayout;

    public UserRightsFragment() {
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
    public void OnUserChanged(User user) {

    }

    @Override
    public void OnStatusChanged(String status) {

    }

    public void OnOptionClick(User user) {
        Global.syncUserRights(user);
        try {
            Log.i("click", user.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class UserListAdapter extends BaseExpandableListAdapter {

        final List<User> users = new ArrayList<>();

        public UserListAdapter() {}

        public void addUser(final User user) {
            synchronized (users) {
                for (int i = 0; i != users.size(); ++i) {
                    final User u = users.get(i);
                    if (u.getName().compareTo(user.getName()) == 0) {
                        users.remove(i);
                        users.add(i, user);
                        return;
                    }
                }
                users.add(user);
            }
        }

        @Override
        public int getGroupCount() {
            return users.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 2;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return users.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
                    (Activity.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_view_group, null);
                TextView username = (TextView) convertView.findViewById(R.id.username);
                final User user = users.get(groupPosition);
                username.setText(user.getName());
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
                        (Activity.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_view_group_subitem, null);
                final User user = users.get(groupPosition);
                final CheckedTextView row = (CheckedTextView) convertView.findViewById(R.id.checkbox);
                switch (childPosition) {
                    case 0:
                        row.setText("Edit rights");
                        row.setChecked(user.isEditor());
                        row.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                user.setEditorRights(!user.isEditor());
                                row.setChecked(user.isEditor());
                                OnOptionClick(user);
                            }
                        });
                        break;
                    case 1:
                        row.setText("Admin rights");
                        row.setChecked(user.isAdmīn());
                        row.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                user.setAdminRights(!user.isAdmīn());
                                row.setChecked(user.isAdmīn());
                                OnOptionClick(user);
                            }
                        });
                        break;
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
