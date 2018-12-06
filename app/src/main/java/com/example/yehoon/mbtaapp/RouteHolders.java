package com.example.yehoon.mbtaapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteHolders extends RecyclerView.ViewHolder {
    public TextView tv_startStop, tv_arrow, tv_endStop, tv_departureTime, tv_arrow2, tv_arrivalTime;
    public LinearLayout linLay_visible, linLay_hidden;

    public RouteHolders(final View itemView) {
        super(itemView);

        linLay_visible = (LinearLayout) itemView.findViewById(R.id.linLay_visible);
        tv_startStop = (TextView) itemView.findViewById(R.id.tv_startStop);
        tv_arrow = (TextView) itemView.findViewById(R.id.tv_arrow);
        tv_endStop = (TextView) itemView.findViewById(R.id.tv_endStop);

        linLay_hidden = (LinearLayout) itemView.findViewById(R.id.linLay_hidden);
        tv_departureTime = (TextView) itemView.findViewById(R.id.tv_departureTime);
        tv_arrow2 = (TextView) itemView.findViewById(R.id.tv_arrow2);
        tv_arrivalTime = (TextView) itemView.findViewById(R.id.tv_arrivalTime);
    }
}