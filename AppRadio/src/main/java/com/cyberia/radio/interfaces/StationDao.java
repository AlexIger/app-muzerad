package com.cyberia.radio.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cyberia.radio.db.Station;

import java.util.List;


@Dao
public interface StationDao
{
//    @Query("SELECT * FROM station")
//    List<Station> getAll();

    @Query("SELECT * FROM station WHERE category =:key")
    List<Station> getCategory(String key);

//    @Query("SELECT * FROM station WHERE id_station IN (:sids)")
//    List<Station> loadAllByIds(int[] sids);

    @Insert
    void insertAll(Station... stations);

    @Query("DELETE FROM station WHERE category= :key")
    void deleteCategory(String key);

//    @Query("DELETE FROM station")
//    void deleteAll();

    @Query("DELETE FROM station WHERE title=:key")
    void deleteStation(String key);

    @Query("SELECT EXISTS(SELECT * FROM station WHERE title = :key AND category= :category)")
    boolean exists(String key, String category);


//    @Insert
//    void insertStation(Station station);
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    void updateStation (Station station);
//
//    @Delete
//    void deleteStation (Station station);
//
//    @Query("DELETE FROM station WHERE id_station = :sid")
//    void deleteOne(long sid);
//
//    @Query("DELETE FROM station")
//    void deleteAll();
//
//    @Query
//    ("SELECT * FROM station WHERE id_station =:sid")
//     Station getStationByID (int sid);
//
//    @Query
//    ("SELECT * FROM station")
//    List<Station> getAllEntries();

}


