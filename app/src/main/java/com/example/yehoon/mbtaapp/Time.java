package com.example.yehoon.mbtaapp;

import java.io.Serializable;

public class Time implements Serializable {
    private String time, tripID;

    public Time() {}

    public String getTime() {
        return time;
    }
    public void setTIme(String time) {
        this.time = time;
    }

    public String getTripID() {
        return tripID;
    }
    public void setTripID(String tripID) {
        this.tripID = tripID;
    }
}