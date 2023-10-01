package com.cyberia.radio.global;

import com.cyberia.radio.io.ServerLookup;
import com.cyberia.radio.stations.StationsFragmentModel;

import de.sfuhrm.radiobrowser4j.RadioBrowser;

public class RadioStationBrowser
{
    public static RadioBrowser getRadioBrowser()
    {
        return new RadioBrowser(ServerLookup.getCurrentServer(),
                StationsFragmentModel.CONNECTION_TIMEOUT, "RadioMuze/6.9");
    }
}
