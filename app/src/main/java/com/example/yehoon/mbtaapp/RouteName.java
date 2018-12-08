package com.example.yehoon.mbtaapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import static com.example.yehoon.mbtaapp.Route.TABLE_NAME;

public class RouteName implements Serializable {
    private String transitName, transitID;

    public RouteName() {}

    public String getTransitName() {
        return transitName;
    }
    public void setTransitName(String transitName) {
        this.transitName = transitName;
    }

    public String getTransitID() {
        return transitID;
    }
    public void setTransitID(String transitID) {
        this.transitID = transitID;
    }
}