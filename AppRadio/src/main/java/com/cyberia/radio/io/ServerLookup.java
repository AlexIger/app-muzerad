package com.cyberia.radio.io;

import android.text.TextUtils;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;

public class ServerLookup
{
    private static final String TAG = "ServerLookup";
    public static volatile String currentServer;
    private final static String defaultServer = "https://nl1.api.radio-browser.info";

    public synchronized static String getCurrentServer()
    {
        if (TextUtils.isEmpty(currentServer))
        {
            try
            {
                EndpointDiscovery server = new EndpointDiscovery("Android/13.0");
                Optional<String> url = server.discover();
                currentServer = url.orElse(defaultServer);

                MyPrint.printOut("Server", currentServer);
            } catch (IOException | NoSuchElementException e)
            {
                ExceptionHandler.onException(TAG, e);
            }
        }

        MyPrint.printOut("Server avail", currentServer);
        return currentServer;
    }
}
