package com.example.yehoon.mbtaapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private boolean startValid, endValid;
    private GoogleMap map;
    private LatLng latLng_start, latLng_end;
    private Stop startStop, endStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initToolbar();
        receiveIntent();

        //gets the maps to load
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.the_map);
        mf.getMapAsync(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //this will catch the <- arrow
        //and return to MainActivity
        //needed since we use fragments to map sites
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {    // map is loaded but not laid out yet
        this.map = map;
        map.setOnMapLoadedCallback(this);      // calls onMapLoaded when layout done
        UiSettings mapSettings;
        mapSettings = map.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
    }

    // maps are loaded and this is where I should perform the getMoreInfo() to grab more data
    @Override
    public void onMapLoaded() {
        if(map != null) {
            // code to run when the map has loaded
            getMoreInfo(); // call this --> use a geoCoder to find the location of the eq

            if(startValid && endValid) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(latLng_start);
                builder.include(latLng_end);
                LatLngBounds bound = builder.build();
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 100));
            }
            else if(startValid) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng_start, 16));
            }
            else if(endValid) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng_end, 16));
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

    private void receiveIntent() {
        // This fetches the addresses from a bundle and places them in an ArrayList
        // ArrayList will be used later by GeoCoder
        Intent mapIntent = getIntent();
        Bundle mapBundle = mapIntent.getExtras();

        startStop = (Stop) mapBundle.getSerializable("startStop");
        endStop = (Stop) mapBundle.getSerializable("endStop");
    }

    public void getMoreInfo() {
        startValid = !(startStop.getStopName().equals("Choose"));
        endValid = !(endStop.getStopName().equals("Choose"));

        if(startValid) {
            // used in addMarker below for placing a marker at the Longitude/Latitude spot
            latLng_start = new LatLng(startStop.getLatitutde(), startStop.getLongitude());

            // puts marker icon at location
            map.addMarker(new MarkerOptions()
                    .position(latLng_start)
                    .snippet("Depart here")
                    .title(startStop.getStopName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
        if (endValid) {
            latLng_end = new LatLng(endStop.getLatitutde(), endStop.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(latLng_end)
                    .snippet("Arrive here")
                    .title(endStop.getStopName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        }
    }
}
