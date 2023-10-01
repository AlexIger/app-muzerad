package com.cyberia.radio.country;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cyberia.radio.genres.GenresModel;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.io.ServerLookup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CountriesModel extends GenresModel
{
    private static final String TAG = "CountriesModel";
    private final CountriesFragment presenter;
    private final Context context;
    private final static String SUB_URL = "json/countrycodes";

    public CountriesModel(CountriesFragment _presenter)
    {
        presenter = _presenter;
        context = presenter.getContext();
    }

    public void getCountryNames()
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
            try (InputStream stream = new URL(ServerLookup.getCurrentServer() + SUB_URL).openStream())
            {
                Map<String, Integer> mapCountryCodes = new HashMap<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                List<Country> countries = new Gson().fromJson(reader, new TypeToken<List<Country>>()
                {
                }.getType());

                for (Country country: countries)
                {
                    int count = country.stationcount;
                    if (count > 0)
                        mapCountryCodes.put(country.name.toLowerCase(), count);
                }

                CountriesModel.this.readCountryCodes(mapCountryCodes);

            } catch (IOException e)
            {
                ExceptionHandler.onException(TAG, e);

                if (CountriesModel.this.validatePresenter())
                    presenter.onCountriesAvailable(null);
            }
        });
    }

    private void readCountryCodes(final Map<String, Integer> countriesMap) throws IOException
    {
        MyPrint.printOut("Countries Model", "Entering readCodes");
        List<Country> filteredList = new ArrayList<>();
        List<Country> listOfCountries;
        String COUNTRIES = "countries.json";
        String countryCode;

        try (InputStream inputStream = context.getAssets().open(COUNTRIES);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<Country>>()
            {
            }.getType();
            listOfCountries = gson.fromJson(reader, collectionType);
        }

        for (Country country : listOfCountries)
        {
            countryCode = country.code.toLowerCase(Locale.ENGLISH);

            if (countriesMap.containsKey(countryCode))
            {
                Bitmap bitmap = BitmapFactory.decodeStream(
                        context.getAssets().open("country_flags/" + countryCode + ".png"));

                if (bitmap != null)
                {
                    country.stationcount = countriesMap.get(countryCode);
                    country.icon = bitmap;

                    filteredList.add(country);
                }
            }
        }

        if (validatePresenter())
        {
            presenter.onCountriesAvailable(filteredList);

        }

    }

    private boolean validatePresenter()
    {
        return presenter != null && presenter.isVisible();
    }


    static class Country
    {
        String name;
        String code;
        int stationcount;
        Bitmap icon;

        public String getStationCount()
        {
            return String.valueOf(stationcount);
        }
    }
}


