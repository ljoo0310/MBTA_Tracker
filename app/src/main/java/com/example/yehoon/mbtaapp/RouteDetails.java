package com.example.yehoon.mbtaapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RouteDetails extends AppCompatActivity {
    private List<Stop> stops;
    private List<Transit> transits = new ArrayList<>();
    private ArrayAdapter<String> adapterRoute, adapterStart, adapterEnd;
    private boolean isNewRoute;
    private int index;
    private int initEditLoad = 0; // int to check if need initial load
    private int initSpnRouteLoad = 1; // int to check if FetchRoute needs to initialize spn_route
    private int transitIndex = -1; // to index route spinner
    private Intent intent;
    private Spinner spn_route, spn_start, spn_end;
    private String start, end;
    private String editTransitID = ""; // to pass into FetchStops
    private Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        receiveIntent();
        initToolbar();
        initTabs();
        initSpinners();
        initButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // pressed map icon
            case R.id.action_favorite:
                // Check if spinners are properly selected
                Boolean startSelected = !(spn_start.getSelectedItem().equals("Choose"));
                Boolean endSelected = !(spn_end.getSelectedItem().equals("Choose"));
                // no stops are selected, so do not call MapActivity
                if(!startSelected && !endSelected)
                    Toast.makeText(RouteDetails.this, "Select at least one stop!", Toast.LENGTH_SHORT).show();
                // at least one stop is selected, so okay to call MapActivity
                else {
                    Intent mapIntent = new Intent(RouteDetails.this, MapActivity.class);
                    Bundle mapBundle = new Bundle();

                    Stop startStop = new Stop();
                    String startStopName = spn_start.getSelectedItem().toString();
                    startStop.setStopName(startStopName);
                    if (!startStopName.equals("Choose"))
                        startStop = getStopInfo(startStopName);
                    Stop endStop = new Stop();
                    String endStopName = spn_end.getSelectedItem().toString();
                    endStop.setStopName(endStopName);
                    if (!endStopName.equals("Choose"))
                        endStop = getStopInfo(endStopName);

                    mapBundle.putSerializable("startStop", startStop);
                    mapBundle.putSerializable("endStop", endStop);
                    mapIntent.putExtras(mapBundle);
                    startActivity(mapIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FetchStops extends AsyncTask<Object, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Object... params) {
            String transitID = (String) params[0];
            int transitIndex = (int) params[1];
            String routeNewEdit = (String) params[2];

            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;

            // init for spinners
            if(transitID == null) {
                String[] emptyJSONPair = new String[]{null, null, null, null};
                return emptyJSONPair;
            }
            else {
                try {
                    final String START_BASE_URL = "https://api-v3.mbta.com/stops?";
                    final String API_KEY = "api_key";
                    final String ROUTE = "filter[route]";

                    // Build up your query URI, limiting results to 10 items and printed movies.
                    Uri builtURI = Uri.parse(START_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                            .appendQueryParameter(ROUTE, transitID)
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
            }
            // Return the raw response.
            String[] transitJSONPair = new String[]{routeJSONString, transitID, Integer.toString(transitIndex), routeNewEdit};
            return transitJSONPair;
        }

        @Override
        protected void onPostExecute(String[] s) {
            String routeJSONString = s[0];

            // init for spinners
            if(routeJSONString == null) {
                initSpnRoute();
            }
            else {
                String transitID = s[1];
                int transitIndex = Integer.parseInt(s[2]);
                String routeNewEdit = s[3];
                try {
                    // Convert the response into a JSON object.
                    JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                    // Get the JSONArray of book items.
                    JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of routes

                    // reset stops list
                    stops = new ArrayList<>();

                    // Initialize iterator and results fields.
                    int stopIndex, increment;
                    // if Green Line - D, do reverse order
                    if(transitID.equals("Green-D")) {
                        stopIndex = itemsArray.length() - 1;
                        increment = -1;
                    }
                    // do proper order otherwise
                    else {
                        stopIndex = 0;
                        increment = 1;
                    }
                    // if Green Line - D, then reverse order
                    for(int i = 0; i < itemsArray.length(); i++) {
                        // Get the current item information.
                        JSONObject dataObject = itemsArray.getJSONObject(stopIndex);
                        JSONObject attributeObject = dataObject.getJSONObject("attributes");
                        String name = attributeObject.getString("name");
                        String id = dataObject.getString("id");
                        double latitude = attributeObject.getDouble("latitude");
                        double longitude = attributeObject.getDouble("longitude");

                        Stop stop = new Stop();
                        stop.setStopName(name);
                        stop.setStopID(id);
                        stop.setLatitutde(latitude);
                        stop.setLongitude(longitude);
                        stops.add(stop);
                        stopIndex += increment;
                    }

                    // get stop names
                    ArrayList<String> str_stops = new ArrayList<>();
                    str_stops.add("Choose");
                    for(int i = 0; i < stops.size(); i++)
                        str_stops.add(stops.get(i).getStopName());

                    // create new adapters
                    adapterStart.clear();
                    adapterStart.addAll(str_stops);
                    adapterEnd.clear();
                    adapterEnd.addAll(str_stops);

                    if(routeNewEdit.equals("edit")){
                        int startIndex = -1, endIndex = -1;
                        for (int i = 0; i < stops.size(); i++) {
                            if (stops.get(i).getStopID().equals(route.getStartID()))
                                startIndex = i;
                            if (stops.get(i).getStopID().equals(route.getEndID()))
                                endIndex = i;
                        }
                        spn_route.setSelection(transitIndex + 1);
                        spn_start.setSelection(startIndex + 1);
                        spn_end.setSelection(endIndex + 1);
                        initEditLoad = 1;
                    }
                    Log.d("LUKE", "before initSpnRouteLoad = " + initSpnRouteLoad);
                    if(initSpnRouteLoad > 0) {
                        Log.d("LUKE", "calling initSpnRoute()");
                        initSpnRoute();
                        Log.d("LUKE", "called initSpnRoute()");
                        initSpnRouteLoad--;
                        Log.d("LUKE", "after initSpnRouteLoad = " + initSpnRouteLoad);
                    }
                } catch (Exception e){
                    // If onPostExecute does not receive a proper JSON string,
                    // update the UI to show failed results.
                    e.printStackTrace();
                }
            }
        }
    }

    private void receiveIntent() {
        // check if adding or editing a route
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        isNewRoute = bundle.getBoolean("newRoute");
        // if editing a route, take route & index info
        if(!isNewRoute) {
            index = (int) bundle.getInt("index");
            route = (Route) bundle.getSerializable("route");
        }
        transits = (ArrayList<Transit>) bundle.getSerializable("transits");
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity);
        // back button color fix (black -> white)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void initTabs() {
        // Create an instance of the tab layout from the view.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label2));
        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        initPager(tabLayout);
    }

    private void initPager(TabLayout tabLayout) {
        // Use PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        // This is another example of the adapter pattern.
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initSpinners() {
        // initialize spinners
        spn_route = (Spinner) findViewById(R.id.spn_route);
        spn_start = (Spinner) findViewById(R.id.spn_start);
        spn_end = (Spinner) findViewById(R.id.spn_end);

        // get stop names
        ArrayList<String> str_transits = new ArrayList<>();
        str_transits.add("Choose");
        for (int i = 0; i < transits.size(); i++) {
            str_transits.add(transits.get(i).getTransitName());
        }
        // temporary string arrays for spinner initializations
        // use ArrayLists in order to use adapter.clear()
        ArrayList<String> temp_array = new ArrayList<String>() {
            {
                add("Choose");
            }
        };

        // create adapters for spinners
        adapterRoute = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, str_transits);
        adapterStart = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, temp_array);
        adapterEnd = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, temp_array);

        // assign data to spinner adapters
        adapterRoute.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterStart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterEnd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // assign adapters to spinners
        spn_route.setAdapter(adapterRoute);
        spn_start.setAdapter(adapterStart);
        spn_end.setAdapter(adapterEnd);

        // adding a new route
        if (isNewRoute) {
            spn_start.setEnabled(false);
            spn_end.setEnabled(false);
            // initialization for spn_route listener when no editing route
            Log.d("LUKE", "calling FetchStops for new route");
            new FetchStops().execute(null, -1, null);
        }
        // editing an existing route
        else {
            for (int i = 0; i < transits.size(); i++) {
                editTransitID = transits.get(i).getTransitID();
                if (editTransitID.equals(route.getTransitID())) {
                    transitIndex = i;
                    break;
                }
            }
            // initialization for spn_route listener and editing route
            Log.d("LUKE", "calling FetchStops for editing route");
            new FetchStops().execute(editTransitID, transitIndex, "edit");
        }
    }

    private void initSpnRoute() {
        Log.d("LUKE", "initSpnRoute called");
        spn_route.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    spn_start.setEnabled(false);
                    spn_end.setEnabled(false);
                    spn_start.setSelection(0);
                    spn_end.setSelection(0);
                }
                else {
                    Log.d("LUKE", "start end spinners enabled");
                    spn_start.setEnabled(true);
                    spn_end.setEnabled(true);
                    String transit = transits.get(spn_route.getSelectedItemPosition() - 1)
                            .getTransitID(); // do -1 for "Choose" value
                    if(initEditLoad == 0) { // carry on regular stops update
                        Log.d("LUKE", "initEditLoad = 0");
                        spn_start.setSelection(0);
                        spn_end.setSelection(0);
                        Log.d("LUKE", "FetchStops for new");
                        new FetchStops().execute(transit, 0, "new");
                    }
                    else // initial load of an editing route, so don't do regular stops update
                        initEditLoad--;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }

    private void initButton() {
        Button btnSave = (Button) findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean routeSelected = !(spn_route.getSelectedItem().equals("Choose"));
                Boolean startSelected = !(spn_start.getSelectedItem().equals("Choose"));
                Boolean endSelected = !(spn_end.getSelectedItem().equals("Choose"));

                // Check if spinners are properly selected
                if(!routeSelected)
                    Toast.makeText(RouteDetails.this, "Route not selected!", Toast.LENGTH_SHORT).show();
                else if(!startSelected && !endSelected)
                    Toast.makeText(RouteDetails.this, "Start and end stops not selected!", Toast.LENGTH_SHORT).show();
                else if(!startSelected)
                    Toast.makeText(RouteDetails.this, "Start stop not selected!", Toast.LENGTH_SHORT).show();
                else if(!endSelected)
                    Toast.makeText(RouteDetails.this, "End stop not selected!", Toast.LENGTH_SHORT).show();
                else {
                    int position = isNewRoute? 0 : index; // a new route or an edited route
                    start = spn_start.getSelectedItem().toString();
                    end = spn_end.getSelectedItem().toString();
                    String transitID = transits.get(spn_route.getSelectedItemPosition() - 1).getTransitID();
                    String startID = stops.get(spn_start.getSelectedItemPosition() - 1).getStopID();
                    String endID = stops.get(spn_end.getSelectedItemPosition() - 1).getStopID();

                    // close and return to MainActivity
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putString("start", start);
                    bundle.putString("end", end);
                    bundle.putString("transitID", transitID);
                    bundle.putString("startID", startID);
                    bundle.putString("endID", endID);
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private Stop getStopInfo(String stopName) {
        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            if (stop.getStopName().equals(stopName))
                return stop;
        }
        // SHOULD NOT REACH HERE
        return new Stop();
    }
}
