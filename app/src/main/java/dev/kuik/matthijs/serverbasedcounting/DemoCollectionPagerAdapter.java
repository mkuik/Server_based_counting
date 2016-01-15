package dev.kuik.matthijs.serverbasedcounting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {
    public DemoCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        switch(i) {
            case 0:
                fragment = new SelectServerFragment();
                break;
            case 1:
                fragment = new CounterFragment();
                break;
            case 3:
                fragment = new AdminFragment();
                break;
            default:
                fragment = new PrefsFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch(position) {
            case 0:
                title = "Servers";
                break;
            case 1:
                title = "Teller";
                break;
            case 2:
                title = "Settings";
                break;
            case 3:
                title = "Admin";
                break;
            default:
                title = "null";
                break;
        }
        return title;
    }
}
