package com.example.yehoon.mbtaapp;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAsync extends AsyncTask<Object, Void, List<Route>> {
    private Context context;
    private List<Route> routes = new ArrayList<>();
    private RouteAdapter recyclerViewAdapter;

    public DatabaseAsync(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Route> doInBackground(Object... params) {
        String action = (String) params[0];
        int position = (int) params[1];
        String start = (String) params[2];
        String end = (String) params[3];
        int transitID = (int) params[4];
        int startID = (int) params[5];
        int endID = (int) params[6];

        // initial database load
        if(action == null) {
            List<Route> routeItems = RouteDatabase.getRouteDatabase(context).routeDao().getRoutes();
            return routeItems;
        }
        // add a new route
        else if(action.equals("new")) {
            Route route = new Route();
            route.setStartStop(start);
            route.setEndStop(end);
            route.setTransitID(transitID);
            route.setStartID(startID);
            route.setEndID(endID);

            //add route into the database
            RouteDatabase.getRouteDatabase(context).routeDao().addRoute(route);
        }
        // edit a route
        else if(action.equals("edit")) {
            Route route = RouteDatabase.getRouteDatabase(context).routeDao().getRoutes().get(position);
            RouteDatabase.getRouteDatabase(context).routeDao().updateRoute(route);
        }
        // delete a route
        else if(action.equals("delete")) {
            Route route = RouteDatabase.getRouteDatabase(context).routeDao().getRoutes().get(position);
            RouteDatabase.getRouteDatabase(context).routeDao().deleteRoute(route);
        }

        // Get routes from database
        List<Route> routeItems = RouteDatabase.getRouteDatabase(context).routeDao().getRoutes();
        return routeItems;
    }

    @Override
    protected void onPostExecute(List<Route> items) {
        // Get list of routes from doInBackground()
        routes = items;
        RouteAdapter adapter = MainActivity.recyclerViewAdapter;
        adapter.setRoutes(routes);
        adapter.notifyDataSetChanged();
    }
}
