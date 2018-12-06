package com.example.yehoon.mbtaapp;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable {
    private String transit, startStop, endStop, departureTime, arrivalTime;
    private int transit_id, startStop_id, endStop_id, departureTime_id, arrivalTime_id;

    public Route(String transit, String startStop, String endStop, String departureTime,
                 String arrivalTime,
                 int transit_id, int startStop_id, int endStop_id, int departureTime_id,
                 int arrivalTime_id) {
        this.transit = transit;
        this.startStop = startStop;
        this.endStop = endStop;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.transit_id = transit_id;
        this.startStop_id = startStop_id;
        this.endStop_id = endStop_id;
        this.departureTime_id = departureTime_id;
        this.arrivalTime_id = arrivalTime_id;
    }

    public String getStartStop() {
        return startStop;
    }
    public String getEndStop() {
        return endStop;
    }
    public String getDepartureTime() {
        return departureTime;
    }
    public String getArrivalTime() {
        return arrivalTime;
    }
    public int getTransit_id() {
        return transit_id;
    }
    public int getStartStop_id() {
        return startStop_id;
    }
    public int getEndStop_id() {
        return endStop_id;
    }
}