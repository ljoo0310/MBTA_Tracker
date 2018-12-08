package com.example.yehoon.mbtaapp;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Route.class},version = 1)
public abstract class RouteDatabase extends RoomDatabase  {
    private static final String DB_NAME = "Route_Database.db";
    private static RouteDatabase INSTANCE;
    public abstract RouteDao routeDao();

    public static RouteDatabase getRouteDatabase(Context context) {
        if (INSTANCE == null)
            INSTANCE = Room.databaseBuilder(context, RouteDatabase.class, DB_NAME).build();
        return INSTANCE;
    }
}
