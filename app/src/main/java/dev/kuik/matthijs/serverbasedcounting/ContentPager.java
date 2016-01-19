package dev.kuik.matthijs.serverbasedcounting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class ContentPager extends FragmentPagerAdapter {
    private List<Page> content;

    public ContentPager(FragmentManager fm) {
        super(fm);
        content = new ArrayList<>();
        content.add(new Page("Servers", new ServerDetectorFragment()));
        content.add(new Page("Teller", new CounterFragment()));
        content.add(new Page("Settings", new PrefsFragment()));

        content.add(new Page("Admin", new UserRightsFragment()));
    }

    class Page {
        final String title;
        final Fragment fragment;
        Page(final String title, final Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }

    @Override
    public Fragment getItem(int i) {
        return content.get(i).fragment;
    }

    @Override
    public int getCount() {
        if (Global.getUser() != null && Global.getUser().isAdmīn()) {
            return content.size();
        } else {
            return content.size() - 1;
        }
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return content.get(i).title;
    }
}
