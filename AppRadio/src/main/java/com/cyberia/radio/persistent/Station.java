package com.cyberia.radio.persistent;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.cyberia.radio.model.StationCookie;


@Entity (tableName = "station", indices = {@Index(value = "UUID", unique = true)})
public class Station
{
    @PrimaryKey(autoGenerate = true)
    @NonNull
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

    @ColumnInfo(name = "playlist")
    public boolean isPlayList;


    public Station()
    {
    }

    @Ignore
    public Station(StationCookie station)
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
    public Station(StationCookie station, String category)
    {
        this(station);
        this.category = category;
    }

    @Ignore
    public Station(String title)
    {
        this.title = title;
    }

    @Ignore
    public String getTitle()
    {
        return title;
    }

    @Ignore
    public String getGenre()
    {
        return genre;
    }

    @Ignore
    public String getUrl()
    {
        return url;
    }

    @Ignore
    public String getThumbUrl()
    {
        return thumbUrl;
    }
    @Ignore
    public String getCountry()
    {
        return country;
    }

    public int getStationID()
    {
        return id_station;
    }

}


