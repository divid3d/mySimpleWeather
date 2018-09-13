package com.example.divided.mysimpleweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class DayWeatherAdapter extends FragmentPagerAdapter {

    DayWeatherAdapter( FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TodayWeatherFragment();
        } else {
            return new LaterWeatherFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "TODAY";
            case 1:
                return "LATER";
            default:
                return null;
        }
    }
}