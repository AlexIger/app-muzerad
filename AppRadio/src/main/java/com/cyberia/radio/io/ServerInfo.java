package com.cyberia.radio.io;


import android.text.TextUtils;
import java.util.HashMap;

public class ServerInfo
{
    public static final String ICY_NAME = "name";
    public static final String ICY_GENRE = "genre";
    public static final String ICY_META_INT = "metaint";
    public static final int NON_META_STREAM = 0;
    public static HashMap<String,String> items;

    public ServerInfo()
    {
        if(items != null)
            items.clear();
        else
            items = new HashMap<>();
    }

    public void addItem(String key, String value) {
        items.put(key.toLowerCase(), value);
    }

    public static int getIcyMetaInt()
    {
        String metaint = items.get(ICY_META_INT);

        if(!TextUtils.isEmpty(metaint))
            return Integer.parseInt(metaint);
        else
            return NON_META_STREAM;
    }
}

