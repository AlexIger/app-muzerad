package com.cyberia.radio.constant;

public interface GenreFlags
{
    String CAT_FAVS = "Favorites";
    String CAT_TREND = "Trending";
    String CAT_MUSIC = "Music";
    String CAT_TALK = "Talk";
    String CAT_BYCOUNTRY = "By Country";
    String CAT_BYLANG = "By Language";
    String CAT_RECENT = "History";
    String CAT_SEARCH = "Search";
    String CAT_SHARE = "Shared";
    String CAT_PLAYLIST = "MyPlaylist";

    int FAVS = 0;
    int TREND = 1;
    int MUSIC = 2;
    int SINGLE_STATION = 3;
    int COUNTRY = 4;
    int LANGUAGE = 5;
    int RECENT = 6;
    int SEARCH = 7;
    int PLAYLIST = 8;
    int STATIONS = 9;


    String[] RadioCategories = {
            CAT_FAVS,
            CAT_TREND,
            CAT_MUSIC,
            CAT_TALK,
            CAT_BYCOUNTRY,
            CAT_BYLANG,
            CAT_RECENT,
            CAT_SEARCH,
            CAT_PLAYLIST};
}

