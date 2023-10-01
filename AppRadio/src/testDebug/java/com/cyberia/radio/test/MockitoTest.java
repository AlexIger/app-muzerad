package com.cyberia.radio.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.bylanguage.LanguageFragment;
import com.cyberia.radio.stations.StationsFragmentModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.SearchMode;
import de.sfuhrm.radiobrowser4j.Station;


@RunWith(MockitoJUnitRunner.class)
public class MockitoTest
{
    @Test
    public void test_when_thenReturn()
    {
        MainActivity activity = Mockito.mock(MainActivity.class);

    }

    @Test
    public void test_verify()
    {
        MainActivity activity = Mockito.mock(MainActivity.class);


//        //verify if getName() is never called
//        verify(activity, never()).getName();
//
//        //now call it one time
//        activity.getName();
//
//        //verify if it is called once
//        verify(activity, atLeastOnce()).getName();
//
//        //call getNumber method with a parameter
//        activity.getNumber(1);
//
//        //verify if getNumber was called with parameter 1
//        verify(activity).getNumber(1);
//
//        verify(activity).displayCoverArt("Art");
    }

    @Test
    public void testLang() throws Exception
    {
        LanguageFragment frag = Mockito.mock(LanguageFragment.class);

        doCallRealMethod().when(frag).onListAvailable(null);

        frag.onListAvailable(new TreeMap<>());

        verify(frag, times(1)).onListAvailable(new TreeMap<>());


//        Map<String, Integer> map = new TreeMap<>();
//        map.put("Name", 100);

//        doCallRealMethod().when(frag).onListAvailable(map);

//        doNothing().when(frag).onListAvailable(map);
//        frag.onListAvailable(map);

//        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
//        Mockito.verify(frag).onListAvailable(captor.capture());
//        assertEquals(100, captor.getValue().get("Name"));

//        verifyPrivate(model).invoke("readCountryCodes");

    }

//    @Test
//    public void test_radio_browswer_connection()
//    {
//        StationsFragmentModel model = new StationsFragmentModel(null);
//        RadioBrowser browser = Mockito.mock(RadioBrowser.class);
//
//        List<Station> list = Arrays.asList(new Station());
//
//        when(browser.listStationsBy(Paging.at(0, 2), SearchMode.BYTAG, "rock", null)).thenReturn(list);
//
//        verify(model).readLazy("", 1, 2, 3);
//    }
}