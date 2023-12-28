package com.cyberia.radio.favorites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.cyberia.radio.persistent.Station;
import com.cyberia.radio.persistent.Repository;

import java.util.List;

public class FavsFragmentModel extends AndroidViewModel
{
    private Repository repo;

    public FavsFragmentModel(@NonNull Application application)
    {
        super(application);
        repo = Repository.getInstance();
    }

    public LiveData<List<com.cyberia.radio.persistent.Station>> getAll()
    {
        return repo.getFavLiveData();
    }

    public void deleteFavorite(Station station)
    {
        repo.deleteSingleFavorites(station);
    }

    public void deleteAllFavorites()
    {
        repo.deleteAllFavorites();
    }
}



