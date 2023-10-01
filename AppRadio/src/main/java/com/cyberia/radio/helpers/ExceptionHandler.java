package com.cyberia.radio.helpers;


import android.util.Log;

import com.cyberia.radio.interfaces.Tag;


public class ExceptionHandler
{
    public static void onException(String className, final Exception e)
    {
        if (Debug.EXCEPTION_ON) print(className, e.getClass().getSimpleName(), e);
    }

    public static void print(String location, String exceptionClass, Exception e)
    {
        Log.i(Tag.TAG, "(E) " +
                location +
                " | ExceptionType: " + exceptionClass +
                " | Message: " + e.getMessage());
    }

    public static void onException(String location, int lineNumber, final Exception e)
    {
        if (Debug.EXCEPTION_ON) printWithLine(location, lineNumber, e);
    }

    public static void printWithLine(String location, int lineNumber, Exception e)
    {
        Log.i(Tag.TAG, "(E) " +
                location +
                " | Line number: " + lineNumber +
                " | Message: " + e.getMessage());
    }
}
