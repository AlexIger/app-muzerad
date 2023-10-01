package com.cyberia.radio.history;

import android.text.TextUtils;

import com.cyberia.radio.db.Station;
import com.cyberia.radio.db.StationsDB;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.model.StationCookie;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager
{
    static final int HISTORY_LIST_SIZE = 64;
    static final String DB_TYPE_HISTORY = "muzeradio.history";
    static List<StationCookie> list;


    public static void initHistory()
    {
        list = new ArrayList<>();

           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
               List<Station> listFromDB = StationsDB.getDatabase().dao().getCategory(DB_TYPE_HISTORY);

               for (Station station : listFromDB)
                   list.add(new StationCookie(station));
           });
    }

    public static void addToRecent(StationCookie cookie)
    {
        int index = 0;
        if (!list.isEmpty())
        {
            for (StationCookie element : list)
            {
                if (!TextUtils.isEmpty(cookie.getTitle()) && cookie.getTitle().equalsIgnoreCase(element.getTitle()))
                {
                    list.remove(index);
                    break;
                }
                index++;
            }
        }

        if (list.size() == HISTORY_LIST_SIZE)
            list.remove(0);

        list.add(cookie);
    }

    public static void updateHistory(int position)
    {
        list.remove(position);
    }

    public static List<StationCookie> loadHistory()
    {
        return list;
    }


    public static void updateDB()
    {
        StationsDB db = StationsDB.getDatabase();

           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
               List<Station> stationList = new ArrayList<>();

               for (StationCookie cookie : list)
               {
                   stationList.add(new Station(cookie, DB_TYPE_HISTORY));
               }

               Station[] stationDB = new Station[stationList.size()];

               db.dao().deleteCategory(DB_TYPE_HISTORY);
               db.dao().insertAll(stationList.toArray(stationDB));
           });
    }

    public static void clearAll()
    {
        list.clear();

        StationsDB db = StationsDB.getDatabase();
           MyThreadPool.INSTANCE.getExecutorService().execute(() -> db.dao().deleteCategory(DB_TYPE_HISTORY));
    }
}
