package dev.kuik.matthijs.serverbasedcounting;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity
        implements SelectServerFragment.Adapter, CounterFragment.Adapter, AdminFragment.Adapter
{
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    TextView message;
    ImageView iconImageView;
    PagerTitleStrip title_strip;
    ThemeBackground theme;
    HorizontalScrollView messageScrollView;
    Bitmap iconBitmap;
    String iconBase64;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        message = (TextView) findViewById(R.id.message);
        iconImageView = (ImageView) findViewById(R.id.logo);
        title_strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        theme = (ThemeBackground) findViewById(R.id.theme_background);
        theme.setInacitiveColor(Color.BLACK);
        messageScrollView = (HorizontalScrollView) findViewById(R.id.message_scroll_view);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        getPrefrences();

        if (iconBase64 != null) setHeaderIcon(iconBase64);
        setTheme(null, false);
        mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setPrefrences();
    }

    public void getPrefrences() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        Global.color1 = preferences.getInt("color1", Color.BLACK);
        Global.color2 = preferences.getInt("color2", Color.WHITE);
        iconBase64 = preferences.getString("icon", null);
        final String ip = preferences.getString("ip", "");
        if (ip.compareTo("") != 0) {
            final Integer port = preferences.getInt("port", 0);
            final String hostname = preferences.getString("hostname", "");
            Global.setHost(new ServerAddress(ip, port, hostname));
        }
        Global.counter_value = preferences.getInt("counter", 0);
        Global.submit_value = preferences.getInt("subtotal", 0);
        Global.counter_max_value = preferences.getInt("max", 0);
    }

    public void setPrefrences() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("color1", Global.color1);
        editor.putInt("color2", Global.color2);
        editor.putString("icon", iconBase64);
        if (Global.getHost() != null) {
            editor.putString("ip", Global.getHost().ip);
            editor.putInt("port", Global.getHost().port);
            editor.putString("hostname", Global.getHost().name);
        }
        editor.putInt("count", Global.counter_value);
        editor.putInt("subtotal", Global.submit_value);
        editor.putInt("max", Global.counter_max_value);
        editor.commit();
    }

    @Override
    public void onSelectedAddress(final ServerAddress address) {
        Log.i("serverselect", address.toString());
        getServerTheme(address);
        Global.setHost(address);
    }

    @Override
    public void onCreateServerAddress(final ServerAddress address) {

    }

    public void getServerTheme(final ServerAddress address) {

        JSONObject json = new JSONObject();
        try {
            json.put("primary_color", "");
            json.put("secondary_color", "");
            json.put("icon", "");
        } catch (JSONException e) {
            message.setText(e.toString());
            return;
        }
        ServerCommunicator server = new ServerCommunicator(address) {
            @Override
            protected void onPostExecute(final String response) {
                super.onPostExecute(response);
                if (response == null) {
                    onServerDisconnected(address);
                    return;
                }
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    onServerResponse(address, jsonResponse);
                    final String primary = jsonResponse.getString("primary_color");
                    final String secondary = jsonResponse.getString("secondary_color");
                    iconBase64 = jsonResponse.getString("icon");
                    Global.color1 = Color.parseColor(primary);
                    Global.color2 = Color.parseColor(secondary);
                    setHeaderIcon(iconBase64);
                } catch (JSONException e) {
                    message.setText(e.toString());
                }
            }
        };
        server.execute(json.toString());
    }

    public void setHeaderIcon(final String icon) {
        AsyncTask<String, Integer, Bitmap> task = new AsyncTask<String, Integer, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    byte[] bytes;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                        bytes = Base64.decode(icon, Base64.DEFAULT);
                    } else {
                        bytes = Base64api7.decode(icon, Base64api7.DEFAULT);
                    }
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (Exception e) {
                    Log.e("set header icon", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                iconBitmap = bitmap;
                setTheme(iconBitmap, true);
            }
        };
        task.execute("{}");
    }

    public void setTheme(final Bitmap icon, final boolean animate) {
        iconImageView.setImageBitmap(icon);
        theme.setColor(Global.color1);
        theme.setAccentColor(Global.color2);
        theme.setInacitiveColor(Color.BLACK);
        title_strip.setTextColor(Global.color2);
        if (animate && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            theme.activate(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Window window = getWindow();
                    messageScrollView.setBackgroundColor(Global.color1);
                    message.setBackgroundColor(Global.color1);
                    message.setTextColor(Global.color2);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int color = (Global.color1 != Color.WHITE ? Global.color1 : Global.color2);
                        window.setNavigationBarColor(color);
                        window.setStatusBarColor(color);
                    }
                    setPrefrences();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            theme.activate(null);
            messageScrollView.setBackgroundColor(Global.color1);
            message.setBackgroundColor(Global.color1);
            message.setTextColor(Global.color2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = (Global.color1 != Color.WHITE ? Global.color1 : Global.color2);
                Window window = getWindow();
                window.setNavigationBarColor(color);
                window.setStatusBarColor(color);
            }
            setPrefrences();
        }

    }

    @Override
    public void onServerDisconnected(ServerAddress address) {
        Global.is_in_sync_with_host = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            theme.deactivate(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Window window = getWindow();
                    messageScrollView.setBackgroundColor(Color.BLACK);
                    message.setBackgroundColor(Color.BLACK);
                    message.setTextColor(Color.WHITE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.setNavigationBarColor(Color.BLACK);
                        window.setStatusBarColor(Color.BLACK);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            theme.deactivate(null);
            messageScrollView.setBackgroundColor(Color.BLACK);
            message.setBackgroundColor(Color.BLACK);
            message.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setNavigationBarColor(Color.BLACK);
                window.setStatusBarColor(Color.BLACK);
            }
        }
    }

    @Override
    public void onServerResponse(ServerAddress address, JSONObject response) {
        Global.is_in_sync_with_host = true;
        if (theme.isActive())
            theme.activate(null);
        else
            setTheme(iconBitmap, true);
        String str = response.toString();

        Context context = getApplicationContext();
        CharSequence text = str.length() + "b from " + address.toString();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        message.setText(str);

        Log.i(address.toString(), str);
    }
}

