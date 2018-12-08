package com.example.yehoon.mbtaapp;

import android.content.Context;
import android.os.AsyncTask;

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

        // add a new route
        if (action.equals("new")) {
            Route route = new Route();
            route.setStartStop(start);
            route.setEndStop(end);

            //add route into the database
            RouteDatabase.getRouteDatabase(context).routeDao().addRoute(route);
        }
        /*
            //update route if shouldUpdate is true
            if (shouldUpdate) {
                Route route = routes.get(position);
                route.setTitle(title);
                route.setDescription(detail);
                route.setDate(date);

                //update route into the database
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().updateRoute(route);

            } else {

            }

        } else { //so no update since shouldUpdate == null
            //delete all if postion is = -2, really bad, i should fix this
            if (position == -2)
                //delete all routes  from database
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().dropTheTable();

                //delete route
            else if (position != -1) { //-1 means delete a specific route
                Route route = routes.get(position);

                //delete route from database
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().deleteRoute(route);
            }
        }
        */

        // Get routes from database
        List<Route> routes = RouteDatabase.getRouteDatabase(context).routeDao().getRoutes();
        return routes;
    }

    @Override
    protected void onPostExecute(List<Route> items) {
        // Get list of routes from doInBackground()
        routes = items;
        RouteAdapter adapter = MainActivity.recyclerViewAdapter;
        adapter.setRoutes(routes);
        adapter.notifyDataSetChanged();

        //shows NO EVENTS FOUND when list is empty
        //checkListEmptyOrNot();
    }
}
