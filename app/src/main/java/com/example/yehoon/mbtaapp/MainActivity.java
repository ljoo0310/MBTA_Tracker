package com.example.yehoon.mbtaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RouteAdapter.ClickListener {

    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private List<Route> routes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup recycler view
        setupRecyclerView();

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fragment);
        setSupportActionBar(toolbar);

        // setup floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RouteDetails.class);
                Bundle bundle = new Bundle();
                bundle.putInt("requestCode", 101);
                bundle.putBoolean("newRoute", true);
                intent.putExtras(bundle);
                startActivityForResult(intent, 101);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.showMap) {
            Toast.makeText(MainActivity.this, "Show MBTA map!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.deleteAll) {
            Toast.makeText(MainActivity.this, "Delete All!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.reOrder) {
            Toast.makeText(MainActivity.this, "Re-order routes!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.about) {
            Toast.makeText(MainActivity.this, "What's the app about?", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class DatabaseAsync extends AsyncTask<Object, Void, List<Route>> {
        private RouteAdapter.ClickListener clickListener; // need to get to main

        // Constructor providing a reference to the views in MainActivity
        public DatabaseAsync(RouteAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
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
                List<Route> routeItems = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes();
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

                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().addRoute(route);
            }
            // edit a route
            else if(action.equals("edit")) {
                Route route = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes().get(position);
                route.setStartStop(start);
                route.setEndStop(end);
                route.setTransitID(transitID);
                route.setStartID(startID);
                route.setEndID(endID);
                
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().updateRoute(route);
            }
            // delete a route
            else if(action.equals("delete")) {
                Route route = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes().get(position);
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().deleteRoute(route);
            }

            // Get routes from database
            List<Route> routeItems = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes();
            return routeItems;
        }

        @Override
        protected void onPostExecute(List<Route> items) {
            // Get list of routes from doInBackground()
            routes = items;
            routeAdapter = new RouteAdapter(getApplicationContext(), routes);
            routeAdapter.setClickListener(clickListener); //this is important since need MainActivity.this
            recyclerView.setAdapter(routeAdapter);
        }
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        new DatabaseAsync(MainActivity.this).execute(null, -1, null, null, 0, 0, 0);  //MainActivity.this explain this usage

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {}

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    @Override
    public void onClick(View view, int position) {}

    @Override
    public void onLongClick(View view, int position) {}

    public void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        Intent intent = new Intent(MainActivity.this, RouteDetails.class);
        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", 102);
        bundle.putBoolean("newRoute", false);
        bundle.putInt("index", index);
        bundle.putSerializable("route", route);
        intent.putExtras(bundle);
        startActivityForResult(intent, 102);
    }

    private void deleteRoute(int position) {
        new DatabaseAsync(MainActivity.this).execute("delete", position, null, null, 0, 0, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // receive data from RouteDetails
        if ((requestCode == 101) && (resultCode == Activity.RESULT_OK)) {
            Bundle bundle = data.getExtras();
            int position = bundle.getInt("index");
            String start = bundle.getString("start");
            String end = bundle.getString("end");
            int transitID = bundle.getInt("transitID");
            int startID = bundle.getInt("startID");
            int endID = bundle.getInt("endID");

            new DatabaseAsync(MainActivity.this).execute("new", position, start, end, transitID, startID, endID);
        }
        else if ((requestCode == 102) && (resultCode == Activity.RESULT_OK)) {
            Bundle bundle = data.getExtras();
            int position = bundle.getInt("position");
            String start = bundle.getString("start");
            String end = bundle.getString("end");
            int transitID = bundle.getInt("transitID");
            int startID = bundle.getInt("startID");
            int endID = bundle.getInt("endID");

            new DatabaseAsync(MainActivity.this).execute("edit", position, start, end, transitID, startID, endID);
        }
    }
}
