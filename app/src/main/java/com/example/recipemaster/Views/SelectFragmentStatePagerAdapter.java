package com.example.recipemaster.Views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "SelectFragmentStatePagerAdapter";

    // two lists hold fragments along with user-defined names
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();


    public SelectFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public int getFragmentByName(String fragName){
        return mFragmentTitleList.indexOf(fragName);
    }
}
