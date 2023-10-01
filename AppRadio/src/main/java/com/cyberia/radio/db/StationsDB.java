package com.cyberia.radio.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.interfaces.StationDao;


@Database(entities = {Station.class}, version = 1, exportSchema = false)
public abstract class StationsDB extends RoomDatabase
{
    public static StationsDB INSTANCE;
    public static final String DB_NAME = "muze_radio_db";
//    private static final String DB_NAME = "db_stations"

    public abstract StationDao dao();

    //get DB
    public static synchronized StationsDB getDatabase()
    {
        if (INSTANCE == null)
        {
            INSTANCE = Room.databaseBuilder(MyAppContext.INSTANCE.getAppContext(), StationsDB.class, DB_NAME).build();
        }

        return INSTANCE;
    }
}


