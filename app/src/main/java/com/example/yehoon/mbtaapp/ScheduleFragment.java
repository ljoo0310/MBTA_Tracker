package com.example.yehoon.mbtaapp;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerView;
    private TimeAdapter recyclerViewAdapter;
    private static String[] scheduleDepartures, scheduleArrivals;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_schedule, container, false);

        fetchResources();
        setupRecyclerView(rootView);

        return rootView;
    }

    private void fetchResources() {
        Resources res = getResources();
        scheduleDepartures = res.getStringArray(R.array.fragmentDepartureTimes);
        scheduleArrivals = res.getStringArray(R.array.fragmentArrivalTimes);
    }

    private void setupRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new TimeAdapter(scheduleDepartures, scheduleArrivals);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}


