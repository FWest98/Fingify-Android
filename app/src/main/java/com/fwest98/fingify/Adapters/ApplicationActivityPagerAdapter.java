package com.fwest98.fingify.Adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class ApplicationActivityPagerAdapter extends FragmentPagerAdapter {
    @Getter private List<Fragment> fragments = new ArrayList<>();
    private int limit = 0;

    public ApplicationActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return (limit == 0 || limit > fragments.size()) ? fragments.size() : limit;
    }

    public void removeItem(int i) {
        fragments.remove(i);
        notifyDataSetChanged();
    }

    public void addItem(Fragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    public void setLimit(int limit) {
        this.limit = limit;
        notifyDataSetChanged();
    }
}
