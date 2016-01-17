package dev.kuik.matthijs.serverbasedcounting;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;

import java.util.Objects;

public class PrefsFragment extends PreferenceFragment {

    private Preference ip;
    private Preference port;
    private Preference admin;
    private Preference count;
    private Preference max;

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        view.setBackgroundResource(R.drawable.content_background);
        final int i = (int)getResources().getDimension(R.dimen.page_margin);
        view.setPadding(i, 0, i, i);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        ip = findPreference("ip");
        port = findPreference("port");
        count = findPreference("count");
        max = findPreference("max");

        ip.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                final String ip = (String) obj;
                Log.i("ip pref", ip);
                if (validIP(ip)) {
                    Global.setHost(new ServerAddress(ip, Global.getHost().port, ""));
                    Global.notifyHost();
                    return true;
                } else {
                    return false;
                }
            }
        });

        port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    final int port = Integer.parseInt((String) obj);
                    Log.i("port pref", "" + port);
                    if (validPort(port)) {
                        Global.setHost(new ServerAddress(Global.getHost().ip, port, ""));
                        Global.notifyHost();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            count.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    final int number = Integer.parseInt((String) obj);
                    Log.i("count pref", "" + number);
                    Global.setCounterValue(number);
                    Global.notifyCounter();
                    return true;
                }
            });
            count.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    final int number = Integer.parseInt((String) obj);
                    Log.i("max pref", "" + number);
                    Global.setCounterMaxValue(number);
                    Global.notifyCounter();
                    return true;
                }
            });
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
}
