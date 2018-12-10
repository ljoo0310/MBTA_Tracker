package com.example.yehoon.mbtaapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<String> scheduledDepatures, scheduledArrivals;
    private int mNumOfTabs;
    private ScheduleFragment scheduleFragment;
    private PredictionFragment predictionFragment;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                scheduleFragment = new ScheduleFragment();
                return scheduleFragment;
            case 1:
                predictionFragment = new PredictionFragment();
                return predictionFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public void updateTimes(String transitID, String startStopID, String endStopID, String directionID) {
        scheduleFragment.updateTimes(transitID, startStopID, endStopID, directionID);
    }
}
