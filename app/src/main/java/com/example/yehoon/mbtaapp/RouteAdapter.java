package com.example.yehoon.mbtaapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

public class RouteAdapter extends RecyclerView.Adapter<RouteHolders> {
    private List<Route> routes;
    protected Context context;

    public RouteAdapter(Context context, List<Route> routes) {
        this.context = context;
        this.routes = routes;
    }

    @Override
    public RouteHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_main, parent, false);
        RouteHolders viewHolder = new RouteHolders(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RouteHolders holder, final int position) {
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

        holder.linLay_visible.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showActionsDialog(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // edit route
                if (which == 0)
                    editRoute(routes.get(position), position);
                // delete route
                else
                    deleteRoute(position);
            }
        });
        builder.show();
    }

    private void editRoute(Route route, int index) {
        Intent intent = new Intent(context, RouteDetails.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("newRoute", false);
        bundle.putInt("index", index);
        bundle.putSerializable("route", route);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void deleteRoute(int position) {
        new DatabaseAsync(context).execute("delete", position, null, null, 0, 0, 0);
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    private String randomTime() {
        return "" + randomInt() + randomInt() + ":" + randomInt() + randomInt() + ":" + randomInt() + randomInt();
    }

    private int randomInt() {
        return new Random().nextInt(10);
    }
}