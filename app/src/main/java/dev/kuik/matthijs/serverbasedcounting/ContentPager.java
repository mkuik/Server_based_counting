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
    private final int ADMIN_INDEX = 3;

    public ContentPager(FragmentManager fm) {
        super(fm);
        content = new ArrayList<>();
        content.add(new Page("Servers", new ServerDetectorFragment()));
        content.add(new Page("Teller", new CounterFragment()));
        content.add(new Page("Settings", new PrefsFragment()));
        content.add(new Page("Admin", new UserRightsFragment()));
        content.add(new Page("Handleiding", new InstructionsFragment()));
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
        return i < ADMIN_INDEX || isAdmin() ? content.get(i).fragment : content.get(i + 1).fragment;
    }

    private boolean isAdmin() {
        return Global.getUser() != null && Global.getUser().isAdmīn();
    }

    @Override
    public int getCount() {
        return isAdmin() ? content.size() : content.size() - 1;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return i < ADMIN_INDEX || isAdmin() ? content.get(i).title : content.get(i + 1).title;
    }
}
