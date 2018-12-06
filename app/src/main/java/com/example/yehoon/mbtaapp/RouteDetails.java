package com.example.yehoon.mbtaapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
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
    Spinner spnRoute, spnStart, spnEnd;
    Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        // check if adding or editing a route
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        boolean isNewRoute = bundle.getBoolean("newRoute");
        if(!isNewRoute) { // editing an existing route
            route = (Route) bundle.getSerializable("route");
        }

        initToolbar();
        initTabs();
        initSpinners(isNewRoute);
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
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
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

    private void initSpinners(boolean isNewRoute) {
        // initialize spinners
        spnRoute = (Spinner)findViewById(R.id.spn_route);
        spnStart = (Spinner)findViewById(R.id.spn_start);
        spnEnd = (Spinner)findViewById(R.id.spn_end);

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
        spnRoute.setAdapter(adapterRoute);
        spnStart.setAdapter(adapterStart);
        spnEnd.setAdapter(adapterEnd);

        if(isNewRoute) { // adding a new route
            spnStart.setEnabled(false);
            spnEnd.setEnabled(false);
        }
        else { // editing an existing route
            spnRoute.setSelection(route.getTransit_id() + 1);
            spnStart.setSelection(route.getStartStop_id() + 1);
            spnEnd.setSelection(route.getEndStop_id() + 1);
        }

        spnRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    spnStart.setEnabled(false);
                    spnEnd.setEnabled(false);
                    spnStart.setSelection(0);
                    spnEnd.setSelection(0);
                }
                else {
                    spnStart.setEnabled(true);
                    spnEnd.setEnabled(true);
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
                // Check if the spinners are properly selected
                Boolean routeSelected = spnRoute.getSelectedItem().equals("Choose");
                Boolean startSelected = spnStart.getSelectedItem().equals("Choose");
                Boolean endSelected = spnEnd.getSelectedItem().equals("Choose");
                if(routeSelected || startSelected || endSelected) {
                    Toast.makeText(RouteDetails.this, "Spinner not selected!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RouteDetails.this, "Save button pressed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
