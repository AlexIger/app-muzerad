package com.cyberia.radio.helpers;


import androidx.annotation.NonNull;


public abstract class Queries
{
    public static final String mbURL = "http://www.musicbrainz.org/ws/2/recording/?query=";

    @NonNull
    public static String getMBIDQuery(String _artist, String _song)
    {
        // remove white spaces
        String ARTIST = _artist.replace(' ', '+');
        String SONG = _song.replace(' ', '+');

        return mbURL +
                "artist:" +
                ARTIST +
                "+" +
                "recording:" + SONG +
                "&limit=1&fmt=json";
    }
}