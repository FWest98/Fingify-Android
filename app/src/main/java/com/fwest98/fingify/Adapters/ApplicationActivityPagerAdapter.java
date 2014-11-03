package com.fwest98.fingify.Adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ApplicationActivityPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments = new ArrayList<>();

    public ApplicationActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void removeItem(int i) {
        fragments.remove(i);
        notifyDataSetChanged();
    }

    public void addItem(Fragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }
}
