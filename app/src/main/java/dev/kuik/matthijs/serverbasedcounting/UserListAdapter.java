package dev.kuik.matthijs.serverbasedcounting;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Matthijs Kuik on 14-12-2015.
 */

public class UserListAdapter extends BaseExpandableListAdapter {

    List<User> users;
    List<OnUserOptionListener> listeners = new ArrayList<>();;
    private LayoutInflater inflater;

    public UserListAdapter() {
        users = new ArrayList<>();
    }

    public UserListAdapter(List<User> list) {
        users = list;
    }

    public void addUser(final User user) {
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

    public void clear() {
        users.clear();
    }

    public void addOptionClickListener(OnUserOptionListener listener) {
        listeners.add(listener);
    }

    public void removeOptionClickListener(OnUserOptionListener listener) {
        listeners.remove(listener);
    }

    public boolean isNewUser(final String username) {
        for (final User user : users) {
            if (user.getName().compareTo(username) == 0) {
                return false;
            }
        }
        return true;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
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
                            notifyUserOptionClick(user);
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
                            notifyUserOptionClick(user);
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

    private void notifyUserOptionClick(final User user) {
        for (OnUserOptionListener l : listeners) {
            if (l != null) l.OnOptionClick(user);
        }
    }

    public interface OnUserOptionListener {
        void OnOptionClick(User user);
    }
}

