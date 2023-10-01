package com.cyberia.radio;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.core.splashscreen.SplashScreen;
import androidx.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.helpers.MyPrint;

public class AppRadio extends Application
{
    private static AppRadio instance;
    public static boolean isNightMode;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        enableDayNight();
        MyAppContext.INSTANCE.setAppContext(getApplicationContext());
    }

    public static AppRadio getInstance()
    {
        return AppRadio.instance;
    }

    private void enableDayNight()
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isNightMode = prefs.getBoolean("day_mode", true);

        if (isNightMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}

