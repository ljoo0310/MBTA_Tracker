package com.example.yehoon.mbtaapp;

import java.io.Serializable;

public class Stop implements Serializable {
    private double latitutde, longitude;
    private String stopName, stopID;

    public Stop() {}

    public String getStopName() {
        return stopName;
    }
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopID() {
        return stopID;
    }
    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public double getLatitutde() {
        return latitutde;
    }
    public void setLatitutde(double latitutde) {
        this.latitutde = latitutde;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}