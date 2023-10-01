package com.cyberia.radio.helpers;

import android.util.Log;
import com.cyberia.radio.interfaces.Tag;

public class MyPrint
{
    public static void printOut(String position, String message)
    {
        if (Debug.INFO_ON) Log.i(Tag.TAG, "(I) " + position + ": #" + message);
    }

    public static void print(String position, String message, int lineNumber)
    {
        if (Debug.INFO_ON) Log.i
                (Tag.TAG, "(I) " +
                        position + ": #" + message +
                        ", Line: " + lineNumber);
    }
}

