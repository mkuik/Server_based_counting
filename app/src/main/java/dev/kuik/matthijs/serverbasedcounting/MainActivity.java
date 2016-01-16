package dev.kuik.matthijs.serverbasedcounting;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
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

import java.util.List;

public class MainActivity extends FragmentActivity implements Global.Adapter
{
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    ImageView iconImageView;
    PagerTitleStrip title_strip;
    ThemeBackground theme;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        iconImageView = (ImageView) findViewById(R.id.logo);
        title_strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        theme = (ThemeBackground) findViewById(R.id.theme_background);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        Global.getPrefrences(this);
        Global.setBitmapIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Global.setPrefrences(this);
        Global.removeListener(this);
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
            theme.setPointOfCircleOrigin(new Point(x, y));
            Log.i("center", String.format("%d %d %d %d : %d %d", iconImageView.getTop(),
                    iconImageView.getBottom(), iconImageView.getLeft(), iconImageView.getRight(),
                    x, y));
        }
    }

    public void setTheme(final Bitmap icon, final int color1, final int color2) {
        iconImageView.setImageBitmap(icon);
        title_strip.setTextColor(color2);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            theme.activate(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    setThemeColor(color1, color2);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        } else {
            theme.activate(null);
            setThemeColor(color1, color2);
        }
    }

    public void setThemeColor(final int color1, final int color2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(null, null, color1));
        }
    }

    @Override
    public void OnHostAddressChanged(ServerAddress address) {
        theme.setBackgroundColor(theme.getForegroundColor());
        Global.syncTheme();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ip", address.ip);
        editor.putInt("port", address.port);
        editor.putString("hostname", address.name);
        editor.commit();
    }

    @Override
    public void OnHostResponseRecieved(ServerAddress address, String response) {
        if (theme.getMode() != ThemeBackground.MODE.ON) {
            setTheme(Global.iconBitmap, Global.getColor1(), Global.getColor2());
        } else {
            theme.activate(null);
        }
    }

    @Override
    public void OnHostResponseLost(ServerAddress address, String response) {
        if (!theme.isInactive()) {
            iconImageView.setImageBitmap(null);
            setThemeColor(Color.BLACK, Color.WHITE);
            theme.deactivate(null);
        }
    }

    @Override
    public void OnThemeChanged(Bitmap icon, int color1, int color2) {
        theme.setForegroundColor(color1);
        setTheme(icon, color1, color2);
    }

    @Override
    public void OnCounterValueChanged(int counter, int max) {

    }

    @Override
    public void OnUserListRecieved(List<User> users) {

    }
}

