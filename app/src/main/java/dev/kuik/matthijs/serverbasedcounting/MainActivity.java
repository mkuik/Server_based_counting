package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
    int color1;
    int color2;
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
        setTheme(color1, color2, null, false);
        mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setPrefrences();
    }

    public void getPrefrences() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        color1 = preferences.getInt("color1", Color.BLACK);
        color2 = preferences.getInt("color2", Color.WHITE);
        iconBase64 = preferences.getString("icon", null);
    }

    public void setPrefrences() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("color1", color1);
        editor.putInt("color2", color2);
        if (iconBase64 != null) editor.putString("icon", iconBase64);
        editor.commit();
    }

    @Override
    public void onSelectedAddress(final ServerAddress address) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ip", address.ip);
        editor.putInt("port", address.port);
        editor.commit();
        getServerTheme(address);
    }

    @Override
    public void onCreateServerAddress(final ServerAddress address) {

    }

    public void getServerTheme(final ServerAddress address) {
        final JSONObject json = new JSONObject();
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
                    color1 = Color.parseColor(primary);
                    color2 = Color.parseColor(secondary);
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
                } catch (NullPointerException e) {
                    Log.e("main", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                iconBitmap = bitmap;
                setTheme(color1, color2, iconBitmap, true);
            }
        };
        task.execute("");
    }

    public void setTheme(final int color1, final int color2, final Bitmap icon, final boolean animate) {
        iconImageView.setImageBitmap(icon);
        theme.setColor(color1);
        theme.setAccentColor(color2);
        theme.setInacitiveColor(Color.BLACK);
        title_strip.setTextColor(color2);
        if (animate && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            theme.activate(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Window window = getWindow();
                    messageScrollView.setBackgroundColor(color1);
                    message.setBackgroundColor(color1);
                    message.setTextColor(color2);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int color = (color1 != Color.WHITE ? color1 : color2);
                        window.setNavigationBarColor(color);
                        window.setStatusBarColor(color);
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
            Window window = getWindow();
            messageScrollView.setBackgroundColor(color1);
            message.setBackgroundColor(color1);
            message.setTextColor(color2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = (color1 != Color.WHITE ? color1 : color2);
                window.setNavigationBarColor(color);
                window.setStatusBarColor(color);
            }
        }

    }

    @Override
    public void onServerDisconnected(ServerAddress address) {
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

    }

    @Override
    public void onServerResponse(ServerAddress address, JSONObject response) {
        if (theme.isActive())
            theme.activate(null);
        else
            setTheme(color1, color2, iconBitmap, true);
        String str = response.toString();
        message.setText(str);
        Log.i(address.toString(), str);
    }
}

