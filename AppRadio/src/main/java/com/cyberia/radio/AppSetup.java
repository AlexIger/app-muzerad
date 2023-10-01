package com.cyberia.radio;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyberia.radio.equalizer.EqualizerSettings;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.model.StationCookie;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AppSetup
{
    private static final String TAG = "AppSetup";
    public static final String SETTINGS = "com.cyberia.radio.settings";
    public static final String DEFAULT = "Muze Radio";
    public static final String LAST_STATION = "key_last_played_station";
    public static final String EQUALIZER = "key_radio_equalizer";

    //private static LinkedTreeMap<Object,Object> vault;
    static StationCookie lastStation;
    static EqualizerSettings eqSettings;
//    static boolean mediaButton;


    public static void saveAll()
    {
        //save object
        Gson gson = new Gson();
        SharedPreferences sharedPreferences =
                MyAppContext.INSTANCE.getAppContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);

        sharedPreferences.edit().putString(LAST_STATION, gson.toJson(lastStation)).apply();
        sharedPreferences.edit().putString(EQUALIZER, gson.toJson(eqSettings)).apply();
//        sharedPreferences.edit().putBoolean(MEDIA_BUTTON, mediaButton);

        MyPrint.printOut(TAG, "Settings saved");
    }

    public static void loadData()
    {
        //load shared prefs
        SharedPreferences sharedPreferences =
                MyAppContext.INSTANCE.getAppContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        try
        {
            String json = sharedPreferences.getString(LAST_STATION, "");
            lastStation = gson.fromJson(json, StationCookie.class);

            String jsonEQ = sharedPreferences.getString(EQUALIZER, "");
            eqSettings = gson.fromJson(jsonEQ, EqualizerSettings.class);
        } catch (JsonSyntaxException e)
        {
            ExceptionHandler.onException("AppSetup", e);
        }

        MyPrint.printOut(TAG, "Settings loaded");
    }

    public static StationCookie getLastPlayedCookie()
    {
        if (lastStation != null)
            return lastStation;
        else
            return null;
    }

    public static void setLastPlayedCookie(StationCookie cookie)
    {
        lastStation = cookie;
    }

    public static void setEQsettings(EqualizerSettings settings)
    {
        eqSettings = settings;
    }

    public static EqualizerSettings getEQsettings()
    {
        if (eqSettings != null)
            return eqSettings;
        else
            return null;
    }
}
