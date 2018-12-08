package com.example.yehoon.mbtaapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteHolder> {
    private List<Route> routes;
    private ClickListener clickListener;
    protected Context context;

    public RouteAdapter(Context context, List<Route> routes) {
        this.context = context;
        this.routes = routes;
    }

    public class RouteHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public LinearLayout linLay_visible, linLay_hidden;
        public TextView tv_startStop, tv_arrow, tv_endStop, tv_departureTime, tv_arrow2, tv_arrivalTime;

        RouteHolder(final View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(this);

            // initializations for visible portion of row item
            linLay_visible = (LinearLayout) itemView.findViewById(R.id.linLay_visible);
            tv_startStop = (TextView) itemView.findViewById(R.id.tv_startStop);
            tv_arrow = (TextView) itemView.findViewById(R.id.tv_arrow);
            tv_endStop = (TextView) itemView.findViewById(R.id.tv_endStop);

            // initialization for hidden portion of row itme
            linLay_hidden = (LinearLayout) itemView.findViewById(R.id.linLay_hidden);
            tv_departureTime = (TextView) itemView.findViewById(R.id.tv_departureTime);
            tv_arrow2 = (TextView) itemView.findViewById(R.id.tv_arrow2);
            tv_arrivalTime = (TextView) itemView.findViewById(R.id.tv_arrivalTime);
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null) clickListener.onLongClick(v, getAdapterPosition());
            return false;
        }
    }

    @Override
    public RouteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_item_main, parent, false);
        context = parent.getContext();
        return new RouteHolder(v);
    }

    @Override
    public void onBindViewHolder(final RouteHolder holder, final int position) {
        // temporary random times
        final String departure = randomTime() + "\n" + randomTime() + "\n" + randomTime();
        final String arrival = randomTime() + "\n" + randomTime() + "\n" + randomTime();

        // setup for visible portion of row item
        holder.tv_startStop.setText(routes.get(position).getStartStop());
        holder.tv_endStop.setText(routes.get(position).getEndStop());
        holder.tv_departureTime.setText(departure);
        holder.tv_arrivalTime.setText(arrival);

        // setup for hidden portion of row item
        holder.linLay_hidden.setVisibility(View.INVISIBLE);
        holder.tv_departureTime.setVisibility(View.INVISIBLE);
        holder.tv_departureTime.setEnabled(false);
        holder.tv_departureTime.setText("");
        holder.tv_arrow2.setVisibility(View.INVISIBLE);
        holder.tv_arrow2.setEnabled(false);
        holder.tv_arrivalTime.setVisibility(View.INVISIBLE);
        holder.tv_arrivalTime.setEnabled(false);
        holder.tv_arrivalTime.setText("");

        holder.linLay_visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // show hidden row details
            if (holder.linLay_hidden.getVisibility() != View.VISIBLE) {
                holder.linLay_hidden.setVisibility(View.VISIBLE);
                holder.tv_departureTime.setVisibility(View.VISIBLE);
                holder.tv_departureTime.setEnabled(true);
                holder.tv_departureTime.setText(departure);
                holder.tv_arrow2.setVisibility(View.VISIBLE);
                holder.tv_arrow2.setEnabled(true);
                holder.tv_arrivalTime.setVisibility(View.VISIBLE);
                holder.tv_arrivalTime.setEnabled(true);
                holder.tv_arrivalTime.setText(arrival);
            }
            // hide hidden row details
            else {
                holder.linLay_hidden.setVisibility(View.INVISIBLE);
                holder.tv_departureTime.setVisibility(View.INVISIBLE);
                holder.tv_departureTime.setEnabled(false);
                holder.tv_departureTime.setText("");
                holder.tv_arrow2.setVisibility(View.INVISIBLE);
                holder.tv_arrow2.setEnabled(false);
                holder.tv_arrivalTime.setVisibility(View.INVISIBLE);
                holder.tv_arrivalTime.setEnabled(false);
                holder.tv_arrivalTime.setText("");
            }
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    // allows clicks events to be caught
    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private String randomTime() {
        return "" + randomInt() + randomInt() + ":" + randomInt() + randomInt();
    }

    private int randomInt() {
        return new Random().nextInt(10);
    }
}