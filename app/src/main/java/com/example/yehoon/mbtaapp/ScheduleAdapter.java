package com.example.yehoon.mbtaapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleHolders> {
    private String[] scheduleDepartures, scheduleArrivals;

    public ScheduleAdapter(String[] scheduleDepartures, String[] scheduleArrivals) {
        this.scheduleDepartures = scheduleDepartures;
        this.scheduleArrivals = scheduleArrivals;
    }

    @Override
    public ScheduleHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_schedule, parent, false);
        ScheduleHolders viewHolder = new ScheduleHolders(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ScheduleHolders holder, final int position) {
        holder.tv_departureTime.setText(scheduleDepartures[position]);
        holder.tv_arrivalTime.setText(scheduleArrivals[position]);
    }

    @Override
    public int getItemCount() {
        return scheduleDepartures.length;
    }

}