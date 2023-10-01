package com.cyberia.radio.model;


import android.text.TextUtils;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;


public class StationCookie
{

    public static final String NOT_AVAIL_STRING = "N/A";
    private final de.sfuhrm.radiobrowser4j.Station station;

    public StationCookie()
    {
        station = new de.sfuhrm.radiobrowser4j.Station();
    }

    public StationCookie(String title)
    {
        this();
        station.setName(title);
    }

    public StationCookie(de.sfuhrm.radiobrowser4j.Station station)
    {
        this.station = station;
    }

    public StationCookie(com.cyberia.radio.db.Station stationDB)
    {
        this();
        station.setName(stationDB.title);
        station.setFavicon(stationDB.thumbUrl);
        station.setHomepage(stationDB.homepage);
        station.setCountry(stationDB.country);
        station.setUrl(stationDB.url);
        station.setTags(stationDB.genre);
        station.setUrlResolved(stationDB.urlRes);
        station.setLanguage(stationDB.language);
        station.setBitrate(stationDB.bitrate);
        station.setCodec(stationDB.codec);
        station.setStationUUID(UUID.fromString(stationDB.uuid));
    }


    //----------------------------- Fav Icon---------------------//
    public String getThumbUrl()
    {
        return (TextUtils.isEmpty(station.getFavicon())) ? null : checkUrlValidity(station.getFavicon());
    }

    private String checkUrlValidity(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
        } catch (URISyntaxException | MalformedURLException e)
        {
            return null;
        }

        return urlString.toLowerCase();
    }

    //----------------------------- Homepage--------------------//
    public String getHomepage()
    {
//        return station.getHomepage();

        return TextUtils.isEmpty(
                station.getHomepage()) ? NOT_AVAIL_STRING : station.getHomepage();
    }

    //----------------------------- Genre---------------------//
    public String getGenre()
    {
        return TextUtils.isEmpty(
                station.getTags()) ? NOT_AVAIL_STRING : station.getTags();
    }

    //----------------------------- Title---------------------//
    public String getTitle()
    {
        return TextUtils.isEmpty(
                station.getName()) ? NOT_AVAIL_STRING : station.getName();
    }

    //----------------------------- Country---------------------//
    public String getCountry()
    {
        return TextUtils.isEmpty(
                station.getCountry()) ? NOT_AVAIL_STRING : station.getCountry();
    }


    //----------------------------- Url resolved---------------------//
    public String getUrlResolved()
    {
        return station.getUrlResolved();
    }

    //----------------------------- Url---------------------//
    public String getUrl()
    {
        return station.getUrl();
    }

    //----------------------------- Language---------------------//
    public String getLanguage()
    {
        return TextUtils.isEmpty(
                station.getLanguage()) ? NOT_AVAIL_STRING : station.getLanguage();
    }

    //----------------------------- Bitrate---------------------//
    public int getBitrate()
    {
        return station.getBitrate();
    }

    //----------------------------- Codec---------------------//
    public String getCodec()
    {
        return TextUtils.isEmpty(
                station.getCodec()) ? NOT_AVAIL_STRING : station.getCodec();
    }

    //----------------------------- uuid---------------------//
    public String getUuid()
    {
        return TextUtils.isEmpty(
                station.getStationUUID().toString()) ? NOT_AVAIL_STRING : station.getStationUUID().toString();
    }
}
