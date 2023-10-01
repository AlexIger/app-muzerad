package com.cyberia.radio.db;


import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.model.StationCookie;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


@Entity
public class Station
{
    @PrimaryKey(autoGenerate = true)
    public int id_station;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "genre")
    public String genre;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "url_resolved")
    public String urlRes;

    @ColumnInfo(name = "url_thumb")
    public String thumbUrl;

    @ColumnInfo(name = "country")
    public String country;

    @ColumnInfo(name = "UUID")
    public String uuid;

    @ColumnInfo(name = "language")
    public String language;

    @ColumnInfo(name = "bitrate")
    public int bitrate;

    @ColumnInfo(name = "codec")
    public String codec;

    @ColumnInfo(name = "homepage")
    public String homepage;

    @ColumnInfo(name = "category")
    public String category;


    public Station(){}

    final static String TAG = "Station";

    @Ignore
    public Station (StationCookie station)
    {
        this.title = station.getTitle();
        this.url = station.getUrl();
        this.urlRes = station.getUrlResolved();
        this.genre = station.getGenre();
        this.thumbUrl = station.getThumbUrl();
        this.country = station.getCountry();
        this.homepage = station.getHomepage();
        this.uuid = station.getUuid();
        this.language = station.getLanguage();
        this.bitrate = station.getBitrate();
        this.codec = station.getCodec();
    }

    @Ignore
    public Station (StationCookie station, String category)
    {
        this(station);
        this.category = category;
    }

    @Ignore
    public Station (String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public String getGenre()
    {
        return genre;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUrlRes()
    {
        return urlRes;
    }

    public String getThumbUrl()
    {
        if (TextUtils.isEmpty(thumbUrl))
        {
            return null;
        } else
        {
            return checkUrlValidity(thumbUrl);
        }
    }

    private String checkUrlValidity(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
        }
        catch (URISyntaxException | MalformedURLException e)
        {
            ExceptionHandler.onException(TAG, e);
            return null;
        }

        return urlString.toLowerCase();
    }

    public String getCountry()
    {
        return country;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getLanguage()
    {
        return language;
    }

    public int getBitrate()
    {
        return bitrate;
    }

    public String getCodec()
    {
        return codec;
    }

    public String getHomepage()
    {
        return homepage;
    }


}


