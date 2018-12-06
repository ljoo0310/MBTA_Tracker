package com.example.yehoon.mbtaapp;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private ArrayList<Route> routes = new ArrayList<Route>();
    private RouteAdapter recyclerViewAdapter;
    private static String[] transits, startStops, endStops, departureTimes, arrivalTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RouteDetails.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("newRoute", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Resources res = getResources();
        transits = res.getStringArray(R.array.transits);
        startStops = res.getStringArray(R.array.startStops);
        endStops = res.getStringArray(R.array.endStops);
        departureTimes = res.getStringArray(R.array.departureTimes);
        arrivalTimes = res.getStringArray(R.array.arrivalTimes);

        initControls();
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

    private void initControls() {
        int size = startStops.length;
        for (int i = 0; i< size; i++) {
            routes.add(new Route(transits[0], startStops[i], endStops[i], departureTimes[i],
                    arrivalTimes[i],
                    0, i, i, i, i));
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new RouteAdapter(this, routes);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /*
    private void editRoute(int position) {
        Toast.makeText(MainActivity.this, " Editing route #:" + position, Toast.LENGTH_SHORT).show();
        Intent intent_edit = new Intent(MainActivity.this, RouteDetails.class);
        Bundle bundle_edit = new Bundle();
        bundle_edit.putSerializable("route", routes.get(position));
        intent_edit.putExtras(bundle_edit);
        startActivity(intent_edit);
    }
    */
    /*
    private void deleteRoute(int position) {
        Toast.makeText(MainActivity.this, " Deleting route #:" + position, Toast.LENGTH_SHORT).show();
    }
    */
}
