//package com.cyberia.radio.history;
//
//import android.text.TextUtils;
//
//import com.cyberia.radio.persistent.Station;
////import com.cyberia.radio.db.StationsDB;
//import com.cyberia.radio.global.MyThreadPool;
//import com.cyberia.radio.model.StationCookie;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class HistoryManager
//{
//    static final int HISTORY_LIST_SIZE = 64;
//    static final String DB_TYPE_HISTORY = "muzeradio.history";
//    static volatile List<StationCookie> list;
//
//
//    //loads history from DB on app start
//    public static void initHistory()
//    {
////        list = new ArrayList<>();
////
////           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
////               List<Station> listFromDB = StationsDB.getDatabase().dao().getCategory(DB_TYPE_HISTORY);
////
////               for (Station station : listFromDB)
////                   list.add(new StationCookie(station));
////           });
//    }
//
//    //add a new station to history list
//    public static void addToRecent(StationCookie cookie)
//    {
//        int index = 0;
//
//        // If the list already contains the station, this will remove it and add it again
//        // to have the last added station to be at the bottom of the list.
//        if (list != null && !list.isEmpty())
//        {
//            for (StationCookie element : list)
//            {
//                if (!TextUtils.isEmpty(cookie.getTitle()) && cookie.getTitle().equalsIgnoreCase(element.getTitle()))
//                {
//                    list.remove(index);
//                    break;
//                }
//                index++;
//            }
//        }
//
//        //If the list goes over 64 elements, this will remove the first element.
//        if (list.size() == HISTORY_LIST_SIZE)
//            list.remove(0);
//
//        list.add(cookie);
//    }
//
//    //Removes element from the history list
//    public static void updateHistory(int position)
//    {
//        list.remove(position);
//    }
//
//    //Returns the list of history for the History model
//    public static List<StationCookie> loadHistory()
//    {
//        return list;
//    }
//
//
//    public static void updateHistoryDB()
//    {
//        StationsDB db = StationsDB.getDatabase();
//
//           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
//
//               Station[]  stations = new Station[list.size()];
//
//               int index = 0;
//               for (StationCookie cookie : list)
//               {
//                   stations[index] = new Station(cookie, DB_TYPE_HISTORY);
//                   index++;
//               }
//               db.dao().deleteCategory(DB_TYPE_HISTORY);
//               db.dao().insertAll(stations);
//           });
//    }
//
//    public static void clearAll()
//    {
//        list.clear();
//
//        StationsDB db = StationsDB.getDatabase();
//           MyThreadPool.INSTANCE.getExecutorService().execute(() -> db.dao().deleteCategory(DB_TYPE_HISTORY));
//    }
//}
