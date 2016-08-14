package dev.kuik.matthijs.serverbasedcounting;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends FragmentActivity implements Global.Adapter, Com.Adapter
{
    ContentPager contentPager;
    ViewPager viewPager;
    ImageView iconImageView;
    PagerTitleStrip title_strip;
    ThemeBackground theme;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentPager = new ContentPager(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        iconImageView = (ImageView) findViewById(R.id.logo);
        title_strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        theme = (ThemeBackground) findViewById(R.id.theme_background);
        viewPager.setAdapter(contentPager);

        Global.getPrefrences(this);
        contentPager.notifyDataSetChanged();
        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.addListener(this);
        Com.addListener(this);
        Global.notifyTheme();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Global.setPrefrences(this);
        Global.removeListener(this);
        Com.removeListener(this);
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            setRippleOrigin();
        }
    }

    public void setRippleOrigin() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            final int x = (iconImageView.getLeft() + iconImageView.getRight()) / 2;
            final int y = (iconImageView.getTop() + iconImageView.getBottom()) / 2;
            theme.setCenter(new Point(x, y));
            Log.i("center", String.format("%d %d %d %d : %d %d", iconImageView.getTop(),
                    iconImageView.getBottom(), iconImageView.getLeft(), iconImageView.getRight(),
                    x, y));
        }
    }

    public void setTheme(final Bitmap icon, final int color1, final int color2) {
        theme.setForegroundColor(color1);
        iconImageView.setImageBitmap(icon);
        theme.activate();
        setThemeColor(color1, color2);
    }

    public void setThemeColor(final int color1, final int color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(null, null, color1));
            getWindow().setNavigationBarColor(color1);
            getWindow().setStatusBarColor(color1);
        }
        title_strip.setTextColor(color2);
        theme.setAccentColor(color2);
    }

    @Override
    public void OnHostAddressChanged(Counter address) {
        Global.setPrefrences(this);
    }

    @Override
    public void comFailed() {
        if (theme.isActive()) {
            iconImageView.setImageBitmap(null);
            setThemeColor(Color.BLACK, Color.WHITE);
            theme.deactivate();
        }
    }

    @Override
    public void comPing() {
        if (theme.isActive()) {
            if (!theme.isGoingActive()) {
                theme.ping();
            }
        } else {
            setTheme(Global.getIcon(), Global.getColor1(), Global.getColor2());
        }
    }

    @Override
    public void OnThemeChanged(Bitmap icon, int color1, int color2) {
        setTheme(icon, color1, color2);
    }

    @Override
    public void OnCounterValueChanged(int counter, int max) {

    }

    @Override
    public void OnUserChanged(User user) {
        contentPager.notifyDataSetChanged();
    }

}

