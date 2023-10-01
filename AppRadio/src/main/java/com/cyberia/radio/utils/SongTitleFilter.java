package com.cyberia.radio.utils;

import android.util.Pair;


public class SongTitleFilter
{
    final static int ARTIST = 0;
    final static int SONG = 1;
    final static Object lock = new Object();

    public static Pair<String, String> filterArtistSong(String artistSong)
    {
        synchronized (lock)
        {
            String[] pair =  artistSong.split(" - ");

            if(pair.length > 1)
                {
                    String artist = pair[ARTIST];
                    String song = pair[SONG];

                    artist = artist.replace("feat.", "").replace("feat", "").replace("ft", "").replace("ft.", "");

                    return Pair.create(artist, song);

            }
        }
        return null;
    }
}
