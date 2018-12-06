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

public class RouteAdapter extends RecyclerView.Adapter<RouteHolders> {
    public ArrayList<Route> routes;
    protected Context context;

    public RouteAdapter(Context context, ArrayList<Route> routes) {
        this.context = context;
        this.routes = routes;
    }

    @Override
    public RouteHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        RouteHolders viewHolder = new RouteHolders(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RouteHolders holder, final int position) {
        // setup for visible portion of row item
        holder.tv_startStop.setText(routes.get(position).getStartStop());
        holder.tv_endStop.setText(routes.get(position).getEndStop());
        holder.tv_departureTime.setText(routes.get(position).getDepartureTime());
        holder.tv_arrivalTime.setText(routes.get(position).getArrivalTime());

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
            Toast.makeText(context, "Clicked on tv_startStop.", Toast.LENGTH_SHORT).show();
            // show hidden row details
            if (holder.linLay_hidden.getVisibility() != View.VISIBLE) {
                Toast.makeText(context, "Departure/Arrival times are now visible.", Toast.LENGTH_SHORT).show();
                holder.linLay_hidden.setVisibility(View.VISIBLE);
                holder.tv_departureTime.setVisibility(View.VISIBLE);
                holder.tv_departureTime.setEnabled(true);
                holder.tv_departureTime.setText(routes.get(position).getDepartureTime());
                holder.tv_arrow2.setVisibility(View.VISIBLE);
                holder.tv_arrow2.setEnabled(true);
                holder.tv_arrivalTime.setVisibility(View.VISIBLE);
                holder.tv_arrivalTime.setEnabled(true);
                holder.tv_arrivalTime.setText(routes.get(position).getArrivalTime());
            }
            // hide hidden row details
            else {
                Toast.makeText(context, "Departure/Arrival times are now hidden.", Toast.LENGTH_SHORT).show();
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
                if (which == 0) { // edit route
                    showRouteDetails(routes.get(position)); //, position);
                    //editRoute(position);
                } else { // delete route
                    deleteRoute(position);
                }
            }
        });
        builder.show();
    }

    private void showRouteDetails(Route route) { //, int position) {
        Intent intent = new Intent(context, RouteDetails.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("newRoute", false);
        bundle.putSerializable("route", route);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void deleteRoute(int position) {
        Toast.makeText(context, " Deleting route #:" + position, Toast.LENGTH_SHORT).show();
        // new DatabaseAsync(MainActivity.this).execute(null, position, null, null, null);
    }
    /*
    public void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //edit the route
                    showRouteDetails(false, routes.get(position), position);
                }
                else {
                    //delete route from database
                    deleteRoute(position);
                }
            }
        });
        builder.show();
    }
    */
    /*
    private void deleteRoute(int position) {
        // new DatabaseAsync(MainActivity.this).execute(null, position, null, null, null);
    }
    */
    /*
    private void showRouteDetails(boolean newRoute, Route route, int position) {
        Intent intent = new Intent(context, RouteDetails.class);
        Bundle data = new Bundle();
        data.putInt("myRequestCode", 101);
        data.putBoolean("newRoute", newRoute);
        intent.putExtras(data);
        startActivityForResult(intent, 101);
    }
    */
}