package dev.kuik.matthijs.serverbasedcounting;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class Main extends FragmentActivity implements Global.Adapter, Com.Adapter
{
    ThemeBackground theme;
    ServerDetectorFragment detector;
    CounterFragment counter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        theme = (ThemeBackground) findViewById(R.id.theme_background);
        Global.getPrefrences(this);
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
//            setRippleOrigin();
        }
    }

    public void setRippleOrigin(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final int x = (v.getLeft() + v.getRight()) / 2;
            final int y = (v.getTop() + v.getBottom()) / 2;
            theme.setCenter(new Point(x, y));
            Log.i("center", String.format("%d %d %d %d : %d %d", v.getTop(),
                    v.getBottom(), v.getLeft(), v.getRight(),
                    x, y));
        }
    }

    public void setTheme(final Bitmap icon, final int color1, final int color2) {
        theme.setForegroundColor(color1);
//        iconImageView.setImageBitmap(icon);
        theme.activate();
        setThemeColor(color1, color2);
    }

    public void setThemeColor(final int color1, final int color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(null, null, color1));
            getWindow().setNavigationBarColor(color1);
            getWindow().setStatusBarColor(color1);
        }
//        title_strip.setTextColor(color2);
        theme.setAccentColor(color2);
    }

    @Override
    public void OnHostAddressChanged(Counter address) {

    }

    @Override
    public void comFailed() {
        if (theme.isActive()) {
//            iconImageView.setImageBitmap(null);
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
//        contentPager.notifyDataSetChanged();
    }

}

