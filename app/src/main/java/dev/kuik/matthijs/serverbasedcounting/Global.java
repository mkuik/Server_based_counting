package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matthijs Kuik on 25-12-2015.
 */
public class Global {

    private static final String TAG = "GLOBAL";
    public static List<Adapter> listeners = new ArrayList<>();
    private static Counter host;
    private static User user;

    public static void getPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            try {
                JSONObject jsonobj = new JSONObject(preferences.getString("user", ""));
                setUser(new User(jsonobj));
            } catch (JSONException e) {
                setUser(new User(activity));
            }
            try {
                setHost(new Counter(new JSONObject(preferences.getString("counter", ""))));
            } catch (JSONException e) {
                Log.d(TAG, e.toString());
                setHost(new Counter(0, 0, "#111111", "#ffffff", null));
            }
            notifyTheme();
        }
    }

    public static void setPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            try {
                editor.putString("user", user.toJSON().toString());
            } catch (JSONException | NullPointerException e) {
                Log.e("store user in pref", e.toString());
            }
            try {
                editor.putString("counter", getHost().toJSON().toString());
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            editor.commit();
        }
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Global.user = user;
        notifyUser();
    }

    public static Integer getColor1() {
        return Color.parseColor(getHost().getColor1());
    }

    public static Integer getColor2() {
        return Color.parseColor(getHost().getColor2());
    }

    public static Bitmap getIcon() { return getHost().getIconBitmap(); }

    public static Integer getCounterMaxValue() {
        return getHost().getMax();
    }

    public static void setCounterMaxValue(Integer counter_max_value) {
//        getHost().setMax(counter_max_value);
    }

    public static Integer getCounterValue() {
        return getHost().getCounter();
    }

    public static void setCounterValue(Integer counter_value) {
        getHost().setCounter(counter_value);
    }

    public static Counter getHost() {
        return host;
    }

    public static void setHost(Counter host) {
        Global.host = host;
        try {
            Log.i(TAG, "SET HOST " + host.toJSON().toString());
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
        }
    }

    public static void addListener(Adapter adapter) {
        listeners.add(adapter);
    }

    public static void removeListener(Adapter adapter) {
        listeners.remove(adapter);
    }

    public static void notifyTheme() {
        Log.i("notify", "theme");
        for (Adapter adapter : listeners) {
            if (adapter != null) {
                adapter.OnThemeChanged(getIcon(), getColor1(), getColor2());
            }
        }
    }

    public static void notifyCounter() {
        Log.i("notify", "counter");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnCounterValueChanged(getCounterValue(), getCounterMaxValue());
        }
    }

    public static void notifyHost() {
        Log.i("notify", "host " + getHost().toString());
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostAddressChanged(getHost());
        }
    }

    public static void notifyUser() {
        Log.i("notify", "user " + getUser().toString());
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnUserChanged(getUser());
        }
    }

    public interface Adapter {
        void OnHostAddressChanged(Counter address);
        void OnThemeChanged(Bitmap icon, int color1, int color2);
        void OnCounterValueChanged(int counter, int max);
        void OnUserChanged(User user);
    }
}

