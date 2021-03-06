package com.example.yehoon.mbtaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RouteAdapter.ClickListener {
    private boolean transitsLoaded = false;
    private ArrayList<String> departTimes = new ArrayList<>(), arriveTimes = new ArrayList<>();
    private List<Transit> transits = new ArrayList<>();
    private List<Route> routes = new ArrayList<>();
    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private TextView noRoutesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // show if no routes found
        noRoutesFound = (TextView) findViewById(R.id.tv_no_routes_found);

        // setup recycler view
        setupRecyclerView();

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fragment);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // fetch routes
        new FetchRoutes().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!transitsLoaded)
                    Toast.makeText(MainActivity.this, "Transits not loaded yet...\nPlease wait and try again", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(MainActivity.this, RouteDetails.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("requestCode", 101);
                    bundle.putBoolean("newRoute", true);
                    bundle.putSerializable("transits", (Serializable) transits);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 101);
                }
            }
        });
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
            Toast.makeText(MainActivity.this, "clicked on showMap!", Toast.LENGTH_SHORT).show();
            Intent webIntent = new Intent();
            webIntent.setAction(Intent.ACTION_VIEW);
            webIntent.setData(Uri.parse("https://www.mbta.com/maps"));
            if (webIntent.resolveActivity(getPackageManager()) != null) //test to see if input leads to a valid url
                startActivity(webIntent);
        } else if (id == R.id.deleteAll)
            new DatabaseAsync(MainActivity.this).execute("deleteAll", 0, null, null, null, null, null);
         /*else if (id == R.id.reOrder) {
            Toast.makeText(MainActivity.this, "Re-order routes!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.about) {
            Toast.makeText(MainActivity.this, "What's the app about?", Toast.LENGTH_SHORT).show();
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        // initial database load
        new DatabaseAsync(MainActivity.this).execute(null, -1, null, null, null, null, null);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                editRoute(routes.get(position), position);
            }

            @Override
            public void onLongClick(View view, int position) {
                deleteRoute(position);
                Toast.makeText(MainActivity.this, "Deleting route", Toast.LENGTH_SHORT).show();
                //showActionsDialog(position);
            }
        }));
    }

    private void updateRecyclerView(List<Route> items, RouteAdapter.ClickListener clickListener) {
        // Get list of routes from doInBackground()
        routes = items;
        routeAdapter = new RouteAdapter(getApplicationContext(), routes, departTimes, arriveTimes);
        routeAdapter.setClickListener(clickListener); //this is important since need MainActivity.this
        recyclerView.setAdapter(routeAdapter);
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
            String startName = (String) params[2];
            String endName = (String) params[3];
            String transitID = (String) params[4];
            String startID = (String) params[5];
            String endID = (String) params[6];

            // initial database load
            if(action == null) {
                List<Route> routeItems = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes();
                return routeItems;
            }
            // add a new route
            else if(action.equals("new")) {
                Route route = new Route();
                route.setStartStop(startName);
                route.setEndStop(endName);
                route.setTransitID(transitID);
                route.setStartID(startID);
                route.setEndID(endID);

                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().addRoute(route);
            }
            // edit a route
            else if(action.equals("edit")) {
                Route route = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes().get(position);
                route.setStartStop(startName);
                route.setEndStop(endName);
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
            // delete all routes
            else if(action.equals("deleteAll")) {
                RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().dropTheTable();
            }

            // Get routes from database
            List<Route> routeItems = RouteDatabase.getRouteDatabase(getApplicationContext()).routeDao().getRoutes();
            return routeItems;
        }

        @Override
        protected void onPostExecute(List<Route> items) {

            updateRecyclerView(items, clickListener);

            // show "No routes found!" if empty
            checkListEmptyOrNot();
        }
    }

    private class FetchRoutes extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;
            try {
                final String ROUTE_BASE_URL =  "https://api-v3.mbta.com/routes?";
                final String API_KEY = "api_key";
                final String TYPE = "type";
                final String SORT = "sort";
                final String MAX_RESULTS = "page[limit]";

                // Build up your query URI, limiting results to 10 items and printed movies.
                Uri builtURI = Uri.parse(ROUTE_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                        .appendQueryParameter(TYPE, "0")
                        .appendQueryParameter(SORT, "long_name")
                        .appendQueryParameter(MAX_RESULTS, "4")
                        .build();

                URL requestURL = new URL(builtURI.toString());

                // Open the network connection.
                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Get the InputStream.
                InputStream inputStream = urlConnection.getInputStream();

                // Read the response string into a StringBuilder.
                StringBuilder builder = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // but it does make debugging a *lot* easier if you print out the completed buffer for debugging.
                    builder.append(line + "\n");
                    publishProgress();
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    // return null;
                    return null;
                }
                routeJSONString = builder.toString();
            } catch (IOException e) { // Catch errors.
                e.printStackTrace();
            } finally { // Close the connections.
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Return the raw response.
            return routeJSONString;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(s); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of routes

                // Initialize iterator and results fields.
                int i = 0;
                // Look for results in the items array
                while (i < itemsArray.length()) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(i);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    String name = attributeObject.getString("long_name");
                    String id = dataObject.getString("id");

                    Transit transit = new Transit();
                    transit.setTransitName(name);
                    transit.setTransitID(id);
                    transits.add(transit);
                    i++;
                }
                transitsLoaded = true;
            } catch (Exception e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                e.printStackTrace();
            }
        }
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
        bundle.putSerializable("transits", (Serializable) transits);
        intent.putExtras(bundle);
        startActivityForResult(intent, 102);
    }

    private void deleteRoute(int position) {
        new DatabaseAsync(MainActivity.this).execute("delete", position, null, null, null, null, null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // new route receieved from RouteDetails
        if ((requestCode == 101) && (resultCode == Activity.RESULT_OK)) {
            Bundle bundle = data.getExtras();
            int position = bundle.getInt("index");
            String startName = bundle.getString("startName");
            String endName = bundle.getString("endName");
            String transitID = bundle.getString("transitID");
            String startID = bundle.getString("startID");
            String endID = bundle.getString("endID");
            departTimes = bundle.getStringArrayList("departTimes");
            arriveTimes = bundle.getStringArrayList("arriveTimes");

            new DatabaseAsync(MainActivity.this).execute("new", position, startName, endName, transitID, startID, endID);
        }
        // edit route received from RouteDetails
        else if ((requestCode == 102) && (resultCode == Activity.RESULT_OK)) {
            Bundle bundle = data.getExtras();
            int position = bundle.getInt("position");
            String startName = bundle.getString("startName");
            String endName = bundle.getString("endName");
            String transitID = bundle.getString("transitID");
            String startID = bundle.getString("startID");
            String endID = bundle.getString("endID");
            arriveTimes = bundle.getStringArrayList("arriveTimes");

            new DatabaseAsync(MainActivity.this).execute("edit", position, startName, endName, transitID, startID, endID);
        }
    }

    public void checkListEmptyOrNot() {
        if (routes.isEmpty())
            noRoutesFound.setVisibility(View.VISIBLE);
        else
            noRoutesFound.setVisibility(View.GONE);
    }
}
