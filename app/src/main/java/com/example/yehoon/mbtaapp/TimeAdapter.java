package com.example.yehoon.mbtaapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeHolder> {
    private String[] departureTimes, arrivalTimes;

    public TimeAdapter(String[] departureTimes, String[] arrivalTimes) {
        this.departureTimes = departureTimes;
        this.arrivalTimes = arrivalTimes;
    }

    public class TimeHolder extends RecyclerView.ViewHolder {
        public TextView tv_departureTime, tv_arrivalTime;
        public LinearLayout linLay_schedule;

        public TimeHolder(final View itemView) {
            super(itemView);

            linLay_schedule = (LinearLayout) itemView.findViewById(R.id.linLay_schedule);
            tv_departureTime = (TextView) itemView.findViewById(R.id.tv_departureTime);
            tv_arrivalTime = (TextView) itemView.findViewById(R.id.tv_arrivalTime);
        }
    }

    @Override
    public TimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_times, parent, false);
        return new TimeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeHolder holder, int position) {
        holder.tv_departureTime.setText(departureTimes[position]);
        holder.tv_arrivalTime.setText(arrivalTimes[position]);
    }

    @Override
    public int getItemCount() {
        return departureTimes.length;
    }
}