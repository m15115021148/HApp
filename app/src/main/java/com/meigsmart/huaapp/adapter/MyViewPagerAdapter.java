package com.meigsmart.huaapp.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.meigsmart.huaapp.fragment.BaseFragment;

public class MyViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<BaseFragment> mMenuList;//fragment集合

    public MyViewPagerAdapter(FragmentManager fm, ArrayList<BaseFragment> mMenuList) {
        super(fm);
        this.mMenuList = mMenuList;
    }

    @Override
    public Fragment getItem(int position) {
        return mMenuList.get(position);
    }

    @Override
    public int getCount() {
        return mMenuList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}
