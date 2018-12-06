package com.example.yehoon.mbtaapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeHolders extends RecyclerView.ViewHolder {
    public TextView tv_departureTime, tv_arrivalTime;
    public LinearLayout linLay_schedule;

    public TimeHolders(final View itemView) {
        super(itemView);

        linLay_schedule = (LinearLayout) itemView.findViewById(R.id.linLay_schedule);
        tv_departureTime = (TextView) itemView.findViewById(R.id.tv_departureTime);
        tv_arrivalTime = (TextView) itemView.findViewById(R.id.tv_arrivalTime);
    }
}