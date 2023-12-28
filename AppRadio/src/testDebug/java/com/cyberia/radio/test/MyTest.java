package com.cyberia.radio.test;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.persistent.MuzeDatabase;
import com.cyberia.radio.persistent.StationDao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class MyTest
{
    private StationDao stationDao;
    private MuzeDatabase db;

    @Before
    public void createDb()
    {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, MuzeDatabase.class).build();
        stationDao = db.stationDao();
    }

    @After
    public void closeDb() throws IOException
    {
        db.close();
    }

    @Test
    public void writeUserAndReadInList()
    {

        MyPrint.printOut("test", "DB");

        Assert.assertEquals(101, 100);

    }
}
