package com.example.yehoon.mbtaapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TimeAdapter extends RecyclerView.Adapter<TimeHolders> {
    private String[] departureTimes, arrivalTimes;

    public TimeAdapter(String[] departureTimes, String[] arrivalTimes) {
        this.departureTimes = departureTimes;
        this.arrivalTimes = arrivalTimes;
    }

    @Override
    public TimeHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_times, parent, false);
        TimeHolders viewHolder = new TimeHolders(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TimeHolders holder, final int position) {
        holder.tv_departureTime.setText(departureTimes[position]);
        holder.tv_arrivalTime.setText(arrivalTimes[position]);
    }

    @Override
    public int getItemCount() {
        return departureTimes.length;
    }
}