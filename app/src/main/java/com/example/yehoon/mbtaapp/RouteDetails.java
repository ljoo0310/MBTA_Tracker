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

public class RouteDetails extends AppCompatActivity {
    private ArrayList<Transit> transits = new ArrayList<>();
    private ArrayAdapter<String> adapterRoute, adapterStart, adapterEnd;
    private boolean isNewRoute;
    private int index, transitID, startID, endID;
    private Intent intent;
    private Spinner spn_route, spn_start, spn_end;
    private String start, end;
    private Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

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
            case R.id.action_favorite:
                Toast.makeText(this, "Map button pressed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FetchStops extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String transitID = (String) params[0];
            Log.d("LUKE", "transitID: " + transitID);
            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;
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
                ArrayList<Stop> stops = new ArrayList<>();
                while (i < itemsArray.length()) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(i);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    String name = attributeObject.getString("name");
                    String id = dataObject.getString("id");

                    Stop stop = new Stop();
                    stop.setStopName(name);
                    stop.setStopID(id);
                    stops.add(stop);
                    i++;
                }
                // get stop names
                ArrayList<String> str_stops = new ArrayList<>();
                str_stops.add("Choose");
                for(int j = 0; j < stops.size(); j++) {
                    str_stops.add(stops.get(j).getStopName());
                }

                // create new adapters
                adapterStart.clear();
                adapterStart.addAll(str_stops);
                adapterEnd.clear();
                adapterEnd.addAll(str_stops);
            } catch (Exception e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                e.printStackTrace();
            }
        }
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
        spn_route = (Spinner)findViewById(R.id.spn_route);
        spn_start = (Spinner)findViewById(R.id.spn_start);
        spn_end = (Spinner)findViewById(R.id.spn_end);

        // get stop names
        ArrayList<String> str_transits = new ArrayList<>();
        str_transits.add("Choose");
        for(int j = 0; j < transits.size(); j++) {
            str_transits.add(transits.get(j).getTransitName());
        }
        // temporary string arrays for spinner initializations
        // use ArrayLists in order to use adapter.clear()
        ArrayList<String> temp_array = new ArrayList<String>(){
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

        if(isNewRoute) { // adding a new route
            spn_start.setEnabled(false);
            spn_end.setEnabled(false);
        }
        else { // editing an existing route
            spn_route.setSelection(route.getTransitID());
            spn_start.setSelection(route.getStartID());
            spn_end.setSelection(route.getEndID());
        }

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
                    spn_start.setSelection(0);
                    spn_end.setSelection(0);
                    spn_start.setEnabled(true);
                    spn_end.setEnabled(true);
                    // do -1 for "Choose" value
                    String transit = transits.get(spn_route.getSelectedItemPosition() - 1).getTransitID();
                    new FetchStops().execute(transit);
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
                    transitID = spn_route.getSelectedItemPosition();
                    startID = spn_start.getSelectedItemPosition();
                    endID = spn_end.getSelectedItemPosition();

                    // close and return to MainActivity
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putString("start", start);
                    bundle.putString("end", end);
                    bundle.putInt("transitID", transitID);
                    bundle.putInt("startID", startID);
                    bundle.putInt("endID", endID);
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}
