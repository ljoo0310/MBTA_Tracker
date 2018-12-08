package com.example.yehoon.mbtaapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import static com.example.yehoon.mbtaapp.Route.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
public class Route implements Serializable {

    // ROOM database setup
    public static final String TABLE_NAME = "routes";
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String startStop;
    @ColumnInfo
    private String endStop;
    @ColumnInfo
    private String departureTime;
    @ColumnInfo
    private String arrivalTime;
    @ColumnInfo
    private int transitID;
    @ColumnInfo
    private int startID;
    @ColumnInfo
    private int endID;

    public Route() {}

    @Ignore
    public Route(int id, String startStop, String endStop, String departureTime, String arrivalTime,
                 int transitID, int startID, int endID) {
        this.id = id;
        this.startStop = startStop;
        this.endStop = endStop;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.transitID = transitID;
        this.startID = startID;
        this.endID = endID;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getStartStop() {
        return startStop;
    }
    public void setStartStop(String startStop) {
        this.startStop = startStop;
    }

    public String getEndStop() {
        return endStop;
    }
    public void setEndStop(String endStop) {
        this.endStop = endStop;
    }

    public String getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getTransitID() {
        return transitID;
    }
    public void setTransitID(int transitID) {
        this.transitID = transitID;
    }

    public int getStartID() {
        return startID;
    }
    public void setStartID(int startID) {
        this.startID = startID;
    }

    public int getEndID() {
        return endID;
    }
    public void setEndID(int endID) {
        this.endID = endID;
    }
}