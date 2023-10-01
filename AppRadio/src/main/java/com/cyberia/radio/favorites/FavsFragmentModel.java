package com.cyberia.radio.favorites;

import com.cyberia.radio.db.Station;
import com.cyberia.radio.model.StationCookie;

import java.util.List;

public class FavsFragmentModel
{

    static List<Station> getFavorites()
    {
        return FavoritesManager.getFavorites();
    }

    static void deleteFavorite(StationCookie cookie)
    {
        FavoritesManager.doAccessDB(cookie, FavoritesManager.Perform.DELETE_SELECTED);
    }

    static void clearFavorites()
    {
        FavoritesManager.doAccessDB(null, FavoritesManager.Perform.DELETE_ALL);
    }
}



