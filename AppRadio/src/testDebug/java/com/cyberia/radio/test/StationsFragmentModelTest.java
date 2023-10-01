package com.cyberia.radio.test;

import static com.cyberia.radio.stations.StationsFragmentModel.TRENDING_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.RadioStationBrowser;
import com.cyberia.radio.stations.StationsFragment;
import com.cyberia.radio.stations.StationsFragmentModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Limit;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

public class StationsFragmentModelTest
{
    Station station = new Station();
    List<Station> list;
    StationsFragmentModel model;
    StationsFragmentModel spyModel;
    StationsFragment fragment;
    RadioBrowser browserMock;

    @Before
    public void setup()
    {
        station.setName("BlueFM");
        station.setCountry("Thailand");
        station.setHls("0");
        station.setTags("Rock");
        station.setUrl("www.radiomuze.com");
        station.setLastcheckok(1);
        list = Arrays.asList(station);
        //mocks
        model = new StationsFragmentModel(fragment);
        spyModel = spy(model);
        fragment = mock(StationsFragment.class);
        browserMock = mock(RadioBrowser.class);
    }

    @Test
    public void test_radio_browser_connection()
    {

        try (MockedStatic<RadioStationBrowser> mockRadioStationBrowser = mockStatic(RadioStationBrowser.class))
        {
            mockRadioStationBrowser.when(RadioStationBrowser::getRadioBrowser).thenReturn(browserMock);

//            when(browserMock.listTopVoteStations(Limit.of(1))).thenReturn(list);

//            List<Station> receivedStations = browserMock.listTopVoteStations(Limit.of(1));

//            verify(browserMock, times(1)).listTopVoteStations(Limit.of(1));

//            assertEquals("BlueFM", receivedStations.get(0).getName());

            spyModel.readLazy("Rock", 1, GenreFlags.TREND, 1);
            verify(spyModel).readLazy("Rock", 1, GenreFlags.TREND, 1);
            verify(browserMock, times(1)).listTopVoteStations(Limit.of(TRENDING_LIMIT));
//
            spyModel.loadStations(list);
            verify(spyModel).loadStations(list);
            verify(spyModel, times(1)).display(list);

        }
    }


    @Test
    public void test_radio_browser_connection_arguments()
    {
        Integer first = 1;
        Integer second = 2;
        Integer third = 3;


        StationsFragmentModel otherModel = mock(StationsFragmentModel.class);
        otherModel.readLazy("Rock", first, 2, 3);

        ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> captor2 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captor3 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> captor4 = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(otherModel).readLazy(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture());
        assertEquals("Rock", captor1.getValue());
        assertEquals(first, captor2.getValue());
        assertEquals(second, captor3.getValue());
        assertEquals(third, captor4.getValue());
    }
}


