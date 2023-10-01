package com.cyberia.radio.favorites;

import android.widget.Toast;
import com.cyberia.radio.db.Station;
import com.cyberia.radio.db.StationsDB;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.model.StationCookie;
import java.util.List;


public class FavoritesManager
{
    //    static final String TAG = "FavoritesManager";
    static final String DB_TYPE_FAV = "muzeradio.favorites";
    static final String NO_DB_ACCESS = "Cannot access internal storage";

    public enum Perform
    {
        INSERT,
        DELETE_SELECTED,
        DELETE_ALL
    }


    public static void doAccessDB(final StationCookie cookie, Perform action)
    {
        Station station = null;

        if (cookie != null)
            station = new Station(cookie, DB_TYPE_FAV);

        StationsDB db = StationsDB.getDatabase();

        if (db == null)
        {
            Toast.makeText(MyAppContext.INSTANCE.getAppContext(), NO_DB_ACCESS, Toast.LENGTH_LONG).show();
        } else
        {
            if(station !=null)
            {
                switch (action)
                {
                    case INSERT:
                        if (!db.dao().exists(station.title, DB_TYPE_FAV))
                            db.dao().insertAll(station);
                        break;
                    case DELETE_SELECTED:
                        db.dao().deleteStation(station.title);
//                MyPrint.printOut(TAG, "Deleted fav station: " + station.title);
                        break;
                    case DELETE_ALL:
                        db.dao().deleteCategory(DB_TYPE_FAV);
//                MyPrint.printOut(TAG, "Delete All Favs");
                        break;
                }
            }
        }
    }

    static List<Station> getFavorites()
    {
        return StationsDB.getDatabase().dao().getCategory(DB_TYPE_FAV);
    }
}
