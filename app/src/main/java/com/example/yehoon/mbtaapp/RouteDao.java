package com.example.yehoon.mbtaapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RouteDao {
    @Query("SELECT * FROM " + Route.TABLE_NAME )
    List<Route> getRoutes();

    @Insert
    void addRoute(Route route);

    @Delete
    void deleteRoute(Route route);

    @Update
    void updateRoute(Route route);

    @Query("DELETE FROM " + Route.TABLE_NAME)
    public void dropTheTable();
}
