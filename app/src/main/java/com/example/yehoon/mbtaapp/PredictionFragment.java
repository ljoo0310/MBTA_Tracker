package com.example.yehoon.mbtaapp;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class PredictionFragment extends Fragment {
    private static ArrayList<String> predictionDepartures, predictionArrivals;
    private RecyclerView recyclerView;
    private TimeAdapter recyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_prediction, container, false);

        fetchResources();
        setupRecyclerView(rootView);

        return rootView;
    }

    private void fetchResources() {
        Resources res = getResources();
        String[] resDepartures = res.getStringArray(R.array.fragmentDepartureTimes);
        String[] resArrivals = res.getStringArray(R.array.fragmentArrivalTimes);
        predictionDepartures = new ArrayList<String>();
        predictionArrivals = new ArrayList<String>();
        for(int i = 0; i < resDepartures.length; i++) {
            predictionDepartures.add(resDepartures[i]);
            predictionArrivals.add(resArrivals[i]);
        }
    }

    private void setupRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new TimeAdapter(predictionDepartures, predictionArrivals);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}


