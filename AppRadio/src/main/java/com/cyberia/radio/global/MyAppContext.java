package com.cyberia.radio.global;

import android.content.Context;

public class MyAppContext
{
    public static MyAppContext INSTANCE = new MyAppContext();
    private Context appContext;

    public void setAppContext(Context context)
    {
        appContext = context;
    }

    public Context getAppContext()
    {
        return appContext;
    }
}
