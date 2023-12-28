package com.cyberia.radio.persistent;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StationDao
{
    /*--------------------Insert single station-----------------------------------*/

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insertIntoFavorites(Favorites station);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIntoHistory(History station);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIntoPlaylist(Playlist station);

    /*--------------------Observe data-----------------------------------*/

    @Query("SELECT * FROM favorites")
    LiveData<List<Station>> getAllFavorites();

    @Query("SELECT * FROM history")
    LiveData<List<Station>> getAllHistory();

    @Query("SELECT * FROM playlist")
    LiveData<List<Station>> getAllPlaylist();

    /*--------------------Delete data-----------------------------------*/

    @Query("DELETE FROM favorites WHERE title = :title")
    void deleteFromFavorites(String title);

    @Query("DELETE FROM favorites")
    void deleteAllFavorites();

    @Query("DELETE FROM history WHERE title = :title")
    void deleteFromHistory(String title);

    @Query("DELETE FROM history")
    void deleteAllHistory();

    @Query("DELETE FROM playlist WHERE title = :title")
    void deleteFromPlaylist(String title);

    @Query("DELETE FROM playlist")
    void deleteAllPlaylist();

}


