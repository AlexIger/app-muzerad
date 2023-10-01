package com.cyberia.radio.bylanguage;

import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.global.RadioStationBrowser;
import com.cyberia.radio.helpers.ExceptionHandler;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class LanguageFragmentModel
{
    private final LanguageFragment presenter;


    public LanguageFragmentModel(LanguageFragment presenter)
    {
        this.presenter = presenter;
    }

    public void readLanguages()
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
            try
            {
                Map<String, Integer> languageMap = RadioStationBrowser.getRadioBrowser().listLanguages();
                presenter.onListAvailable(toTitleCase(languageMap));
            } catch (Exception e)
            {
                ExceptionHandler.onException("LanguageModel", e);

                if (presenter != null && presenter.isVisible())
                    presenter.onModelError("Stations not found");
            }
        });
    }

    private TreeMap<String, Integer> toTitleCase(Map<String, Integer> map)
    {
        String toCaps;
        String raw;
        TreeMap<String, Integer> sortedMap = new TreeMap<>();

        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            raw = entry.getKey();
            if (raw.length() > 3 && entry.getValue() > 2)
            {
                toCaps = raw.toUpperCase(Locale.US).charAt(0) + raw.substring(1);
                sortedMap.put(toCaps, entry.getValue());
            }
        }

        return sortedMap;
    }
}