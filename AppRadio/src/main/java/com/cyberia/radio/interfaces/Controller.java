package com.cyberia.radio.interfaces;

import com.cyberia.radio.model.StationCookie;

public interface Controller
{
    void addHomeScreenFragment();
    void addGenreScreenFragment (int position);
    void addStationScreenFragment (String search, int what);
    void addStationScreenFragment (String search, String name, int what);
    void addCountriesScreenFragment();
    void addLangsFragment();
    void addHistoryFragment();
    void addFavsScreenFragment();
    void addSearchFragment();
    void updatePlaybackStatus(String s);
    void onStationInfoAvailable(StationCookie cookie);

    void updateArtistSongName(String radio);
    void onPlaybackStarted();

    void displayCoverArt(String title);
    void updateAppBarTitle(String title, String subTitle, boolean enableBackButton);
    void setBackButton(boolean isBackButton);
    void slidePanel(int dir);
}
