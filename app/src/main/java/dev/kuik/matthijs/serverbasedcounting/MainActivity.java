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

import java.util.List;

public class MainActivity extends FragmentActivity implements Global.Adapter
{
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    TextView message;
    ImageView iconImageView;
    PagerTitleStrip title_strip;
    ThemeBackground theme;
    HorizontalScrollView messageScrollView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        message = (TextView) findViewById(R.id.message);
        iconImageView = (ImageView) findViewById(R.id.logo);
        title_strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        theme = (ThemeBackground) findViewById(R.id.theme_background);
        messageScrollView = (HorizontalScrollView) findViewById(R.id.message_scroll_view);
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
        messageScrollView.setBackgroundColor(color1);
        message.setBackgroundColor(color1);
        message.setTextColor(color2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = (color1 != Color.WHITE ? color1 : color2);
            Window window = getWindow();
            window.setNavigationBarColor(color);
            window.setStatusBarColor(color);
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
        message.setText(response);
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

