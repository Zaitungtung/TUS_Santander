package com.alce.tus.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.alce.tus.Fragments.Bikes;
import com.alce.tus.Fragments.Home;
import com.alce.tus.Fragments.Lines;
import com.alce.tus.Fragments.Nearby;

import java.util.ArrayList;

/**
 * Adapter for the initial View Pager.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<Fragment> fragments = new ArrayList<>();
    private Fragment currentFragment;

    public PagerAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        fragments.clear();

        SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean busShow = prefs.getBoolean("busShow", true);
        boolean bikeShow = prefs.getBoolean("bikeShow", true);

        fragments.add(new Home());

        if (busShow)
            fragments.add(new Lines());
        if (bikeShow)
            fragments.add(new Bikes());

        fragments.add(new Nearby());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);

        if(FavsAdapter.actionMode!=null)
            FavsAdapter.actionMode.finish();
    }

    /**
     * Get the current fragment
     */
    private Fragment getCurrentFragment() {
        return currentFragment;
    }
}
