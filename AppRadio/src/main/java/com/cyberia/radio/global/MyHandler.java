package com.cyberia.radio.global;

import android.os.Handler;
import android.os.Looper;

public class MyHandler
{
    public static Handler getHandler()
    {
        return new Handler(Looper.getMainLooper());
    }

    public static void post(Runnable r)
    {
        new Handler(Looper.getMainLooper()).post(r);
    }
}

//MyHandler.post(mediaButtonHandler::setCallback);
