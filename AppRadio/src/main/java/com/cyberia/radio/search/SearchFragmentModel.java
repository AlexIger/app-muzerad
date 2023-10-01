package com.cyberia.radio.search;


import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.global.RadioStationBrowser;
import com.cyberia.radio.helpers.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.sfuhrm.radiobrowser4j.FieldName;
import de.sfuhrm.radiobrowser4j.ListParameter;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.SearchMode;
import de.sfuhrm.radiobrowser4j.Station;

public class SearchFragmentModel
{
    private final static String TAG = "SearchFragmentModel";
    private final SearchFragment presenter;
    private final ListParameter param;
    private volatile RadioBrowser browser;


    public SearchFragmentModel(SearchFragment _presenter)
    {
        presenter = _presenter;
        param = ListParameter.create().order(FieldName.NAME);
    }


    public void readStations(final String searchKey)
    {
           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
                try
                {
                    if(browser == null)
                        browser = RadioStationBrowser.getRadioBrowser();

                    Stream<Station> stationStream = browser.listStationsBy(SearchMode.BYNAME, searchKey, param);
                    List<Station> listOfStations = stationStream.collect(Collectors.toList());

                    if (listOfStations.isEmpty())
                        throw new RuntimeException("No station with such key found");
                    else
                    {
                        List<Station> listFiltered = new ArrayList<>();

                        for (Station station:listOfStations)
                        {
                            if (!station.getHls().equalsIgnoreCase("1") && station.getLastcheckok() == 1)
                                listFiltered.add(station);
                        }

                        if (presenter != null)
                            presenter.onStationsAvailable(listFiltered);
                    }

                } catch (Exception e)
                {
                    if (presenter != null)
                        presenter.onStationsAvailable(null);

                    ExceptionHandler.onException(TAG, e);
                }
            });
    }
}

