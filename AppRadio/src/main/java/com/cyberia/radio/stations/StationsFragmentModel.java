package com.cyberia.radio.stations;


import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.RadioStationBrowser;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import de.sfuhrm.radiobrowser4j.FieldName;
import de.sfuhrm.radiobrowser4j.Limit;
import de.sfuhrm.radiobrowser4j.ListParameter;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.SearchMode;
import de.sfuhrm.radiobrowser4j.Station;


public class StationsFragmentModel
{
    //    private static String API_URL = "https://de1.api.radio-browser.info/"; //"https://nl1.api.radio-browser.info";
    private final static String TAG = "StationsFragmentModel";
    private final StationsFragment presenter;
    public static final int DEFAULT_PAGE_SIZE = 128;
    public static final int TRENDING_LIMIT = 256;
    public static final int CONNECTION_TIMEOUT = 10000;
    private final ListParameter param;
    private final AtomicInteger count = new AtomicInteger(0);
    private RadioBrowser browser;


    public StationsFragmentModel(StationsFragment _presenter)
    {
        presenter = _presenter;
        param = ListParameter.create().order(FieldName.NAME);
    }

    public int getCount()
    {
        return count.get();
    }


    public void readLazy(final String searchKey, final int positionStart, int what, int limit)
    {
        if (browser == null)
            browser = RadioStationBrowser.getRadioBrowser();


        try
        {
            List<Station> listOfStations = null;

            if (what == GenreFlags.SINGLE_STATION)
            {
                MyPrint.printOut(TAG, "Single station");
                readSingleStation(searchKey, listOfStations);
            } else if (what == GenreFlags.TREND)
            {
                listOfStations = browser.listTopVoteStations(Limit.of(TRENDING_LIMIT));
            } else if (what == GenreFlags.COUNTRY)
            {
                listOfStations = browser.listStationsBy(Paging.at(positionStart, limit),
                        SearchMode.BYCOUNTRYCODEEXACT, searchKey.toLowerCase(), param);
            } else if (what == GenreFlags.LANGUAGE)
            {
                listOfStations = browser.listStationsBy(Paging.at(positionStart, limit),
                        SearchMode.BYLANGUAGEEXACT, searchKey.toLowerCase(), param);
            } else
            {
                listOfStations = browser.listStationsBy(Paging.at(positionStart, limit),
                        SearchMode.BYTAG, searchKey.toLowerCase(), param);
            }

            loadStations(listOfStations);

        } catch (Exception e)
        {
            ExceptionHandler.onException(TAG, 98, e);
            if (presenter != null && presenter.isVisible())
                presenter.onStationsAvailable(null, 0);
        }
    }

    public void readSingleStation(String searchKey, List<Station> listOfStations)
    {
        UUID uuid = UUID.fromString(searchKey);
        Optional<Station> station = browser.getStationByUUID(uuid);
        List<Station> stationList = new ArrayList<>();
        if (station.isPresent())
        {
            stationList.add(station.get());
            count.set(1);
        }
        display(stationList);
    }

    public void loadStations(List<Station> batch)
    {
        List<Station> listReady = null;

        if (!CollectionUtils.isEmpty(batch))
        {
            listReady = new ArrayList<>();
            count.set(count.get() + batch.size());

            for (Station station : batch)
            {
                if (station.getLastcheckok() == 1 && station.getHls().equalsIgnoreCase("0"))
                    listReady.add(station);
            }

        }
        display(listReady);
    }

    public void display(List<Station> list)
    {
        if (presenter != null && presenter.isVisible())
        {
            presenter.onStationsAvailable(list, getCount());
        }
    }
}


