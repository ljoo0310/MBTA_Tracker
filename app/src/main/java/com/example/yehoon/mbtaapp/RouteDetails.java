package com.example.yehoon.mbtaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class RouteDetails extends AppCompatActivity {
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

        // create adapters for spinners
        ArrayAdapter<CharSequence> adapterRoute = ArrayAdapter.createFromResource(this,
                R.array.transits, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterStart = ArrayAdapter.createFromResource(this,
                R.array.startStopsSpinner, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterEnd = ArrayAdapter.createFromResource(this,
                R.array.endStopsSpinner, android.R.layout.simple_spinner_item);

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
                    spn_start.setEnabled(true);
                    spn_end.setEnabled(true);
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
                    int position;
                    start = spn_start.getSelectedItem().toString();
                    end = spn_end.getSelectedItem().toString();
                    transitID = spn_route.getSelectedItemPosition();
                    startID = spn_start.getSelectedItemPosition();
                    endID = spn_end.getSelectedItemPosition();

                    // add new route to database
                    if(isNewRoute) {
                        position = 0;
                        //new DatabaseAsync(getApplicationContext()).execute("new", 0, start, end, transitID, startID, endID);
                    }
                    // edit route and update database
                    else {
                        position = index;
                        //new DatabaseAsync(getApplicationContext()).execute("edit", index, start, end, transitID, startID, endID);
                    }

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
                    /*
                    Intent intent = new Intent(RouteDetails.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    */
                }
            }
        });
    }
}
