package com.example.yehoon.mbtaapp;

import java.io.Serializable;

public class Stop implements Serializable {
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
}