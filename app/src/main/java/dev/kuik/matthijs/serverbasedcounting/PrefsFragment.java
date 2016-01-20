package dev.kuik.matthijs.serverbasedcounting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PrefsFragment extends Fragment implements AdapterView.OnItemClickListener {

    final String tag = "PrefsFragment";
    private ListItem ip;
    private ListItem port;
    private ListItem timeout;
    private ListItem admin;
    private ListItem count;
    private ListItem max;
    private ListAdapter options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_preferences, viewGroup, false);
        if (view != null) {
            ListView listView = (ListView) view.findViewById(R.id.preference_listview);
            listView.setAdapter(options);
            listView.setOnItemClickListener(this);
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        options = new ListAdapter();

        ip = new ListItem(getActivity());
        ip.setTitle(getResources().getString(R.string.ip_address_preference_title));
        ip.setSubtitle(getResources().getString(R.string.ip_address_preference_subtitle));

        port = new ListItem(getActivity());
        port.setTitle(getResources().getString(R.string.port_address_preference_title));
        port.setSubtitle(getResources().getString(R.string.port_address_preference_subtitle));

        timeout = new ListItem(getActivity());
        timeout.setTitle(getResources().getString(R.string.timeout_preference_title));
        timeout.setSubtitle(getResources().getString(R.string.timeout_preference_subtitle));

        admin = new ListItem(getActivity());
        admin.setTitle(getResources().getString(R.string.admin_rights_preference_title));
        admin.setSubtitle(getResources().getString(R.string.admin_rights_preference_title));

        count = new ListItem(getActivity());
        count.setTitle(getResources().getString(R.string.count_preference_title));
        count.setSubtitle(getResources().getString(R.string.count_preference_subtitle));

        max = new ListItem(getActivity());
        max.setTitle(getResources().getString(R.string.max_preference_title));
        max.setSubtitle(getResources().getString(R.string.max_preference_subtitle));

        options.add(ip);
        options.add(port);
        options.add(timeout);
        options.add(admin);
        options.add(count);
        options.add(max);
    }

    public static boolean validIP(final String ip) {
        try {
            if ( ip == null || ip.compareTo("") == 0 ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean validPort(final int port) {
        return port >= 0 && port <= 65535;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view == ip) {
            showIPDialog();
        } else if (view == port) {
            showPortDialog();
        } else if (view == timeout) {
            showTimeoutDialog();
        } else if (view == admin) {
            showAdminDialog();
        } else if (view == count) {
            showCountDialog();
        } else if (view == max) {
            showMaxDialog();
        }
    }

    private void showIPDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.ip_dialog, null);
        final EditText ipField = (EditText) view.findViewById(R.id.ip_address_dialog_field);
        ipField.setText(Global.getHost().ip);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String text = ipField.getText().toString();
                Log.i(tag, "from ip dialog: " + text);
                if (validIP(text)) {
                    ServerAddress address = new ServerAddress(text, Global.getHost().port, "");
                    Global.setHost(address);
                    Global.syncTheme();
                    Global.notifyHost();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void showPortDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.port_dialog, null);
        final EditText inputField = (EditText) view.findViewById(R.id.poro_address_dialog_field);
        inputField.setText(Integer.toString(Global.getHost().port));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final int port = Integer.valueOf(inputField.getText().toString());
                Log.i(tag, "from port dialog: " + port);
                if (validPort(port)) {
                    ServerAddress address = new ServerAddress(Global.getHost().ip, port, "");
                    Global.setHost(address);
                    Global.syncTheme();
                    Global.notifyHost();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void showTimeoutDialog() {
        int timeout = 300;
        final View view = getActivity().getLayoutInflater().inflate(R.layout.timeout_dialog, null);
        final SeekBar inputField = (SeekBar) view.findViewById(R.id.timeout_dialog_seekbar);
        final TextView message = (TextView) view.findViewById(R.id.timeout_dialog_message);
        final String instruction = message.getText().toString();
        inputField.setProgress(timeout);
        message.setText(instruction + ": " + timeout + "ms");

        inputField.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                message.setText(instruction + ": " + progress + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final int timeout = inputField.getProgress();
                Log.i(tag, "from timeout dialog: " + timeout);
                Global.setDetector_timeout(timeout);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void showAdminDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.admin_dialog, null);
        final EditText inputField = (EditText) view.findViewById(R.id.admin_dialog_field);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String text = inputField.getText().toString();
                Log.i(tag, "from admin dialog: " + text);
                Global.getAdmin(text);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void showCountDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.count_dialog, null);
        final EditText inputField = (EditText) view.findViewById(R.id.count_dialog_field);
        inputField.setText(Global.getCounterValue().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String text = inputField.getText().toString();
                Log.i(tag, "from count dialog: " + text);
                Global.overrideCounter(Integer.valueOf(text));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void showMaxDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.max_dialog, null);
        final EditText inputField = (EditText) view.findViewById(R.id.max_dialog_field);
        inputField.setText(Global.getCounterMaxValue().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String text = inputField.getText().toString();
                Log.i(tag, "from max dialog: " + text);
                Global.overrideMax(Integer.valueOf(text));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    class ListAdapter extends BaseAdapter {

        final String tag = "PreferenceListAdapter";
        final ArrayList<ListItem> options = new ArrayList<>();

        public ListAdapter() { }

        public void add(final ListItem option) {
            synchronized (options) {
                options.add(option);
            }
        }

        @Override
        public int getCount() {
            return options.size();
        }

        @Override
        public Object getItem(int position) {
            return options.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            return options.get(position);
        }
    }
}
