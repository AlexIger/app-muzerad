package com.cyberia.radio;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.cyberia.radio.about.AboutFragment;
import com.cyberia.radio.bylanguage.LanguageFragment;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.country.CountriesFragment;
import com.cyberia.radio.equalizer.EqualizerManager;
import com.cyberia.radio.equalizer.EqualizerSettings;
import com.cyberia.radio.eventbus.Events;
import com.cyberia.radio.favorites.FavsFragment;
import com.cyberia.radio.genres.GenreFragment;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.history.HistoryFragment;
//import com.cyberia.radio.history.HistoryManager;
import com.cyberia.radio.home.HomeFragment;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.io.ConnectionClient;
import com.cyberia.radio.io.CoverArtClient;
import com.cyberia.radio.io.ServerLookup;
import com.cyberia.radio.model.StationCookie;
import com.cyberia.radio.persistent.Repository;
import com.cyberia.radio.player.PlaybackManager;
import com.cyberia.radio.player.PlaybackNotifier;
import com.cyberia.radio.playlist.PlaylistFragment;
//import com.cyberia.radio.playlist.PlaylistManager;
import com.cyberia.radio.radiodetails.RadioDetailFragment;
import com.cyberia.radio.search.SearchFragment;
import com.cyberia.radio.settings.PrefsFragment;
import com.cyberia.radio.slidingpanel.SlidingUpPanelLayout;
import com.cyberia.radio.slidingpanel.SlidingUpPanelLayout.PanelSlideListener;
import com.cyberia.radio.slidingpanel.SlidingUpPanelLayout.PanelState;
import com.cyberia.radio.stations.StationsFragment;
import com.cyberia.radio.utils.StringCapitalizer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class MainActivity extends AppCompatActivity implements Controller
{
    private static final String HOME = "frag_home";
    private static final String FAVORITES = "frag_favs";
    private static final String PLAYLIST = "frag_playlist";
    private static final String STATION = "frag_station";
    private static final String GENRE = "frag_genre";
    private static final String PREFS = "frag_prefs";
    private static final String COUNTRY = "frag_country";
    private static final String ABOUT = "frag_about";
    private static final String LANGS = "frag_langs";
    private static final String HISTORY = "frag_history";
    private static final String RADIO_DETAIL = "frag_radio_detail";
    private static final String SEARCH = "frag_search";
    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    static final String STATION_NOT_SELECTED = "No station selected";

    public static final int PANEL_SLIDE_UP = 0;
    public static final int PANEL_SLIDE_DOWN = 1;

    public static final String MSG_PLAYBACK_STOP = "Stopped";
    public static final String STRING_EMPTY = "";
    public static final int BUTTON_OPACITY = 60;
    static final String STRING_BUFFERING = "Buffering...";
    static final String MSG_CONNECTING = "Opening...";
    static final String MSG_FAIL_CONNECT = "Unable to connect";

    private SlidingUpPanelLayout mLayout;
    private ViewSwitcher switcher;
    private volatile PlaybackManager playbackManager;

    private volatile ProgressBar progressBar;
    private static final int MAX = 100;

    private final AtomicBoolean isTopShown = new AtomicBoolean(false);
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);

    private final AtomicReference<StationCookie> cookieRefs = new AtomicReference<>();

    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    private PowerManager.WakeLock wakelock;
    private WifiManager.WifiLock wifiLock;

    private BroadcastReceiver notificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SplashScreen.installSplashScreen(this);
        // Set up an OnPreDrawListener to the root view.
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener()
                {
                    @Override
                    public boolean onPreDraw()
                    {
                        Thread initServer = new Thread(() -> ServerLookup.getCurrentServer());
                        initServer.start();
                        MyThreadPool.INSTANCE.getExecutorService();
                        content.getViewTreeObserver().removeOnPreDrawListener(this);

                        return true;
                    }
                });

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        createPopup();

        mLayout = findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new PanelSlideListener()
        {
            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState)
            {
                switcher = findViewById(R.id.panel_switcher);

                if (newState.equals(PanelState.EXPANDED))
                {
                    if (!isTopShown.get())
                    {
                        switcher.showNext();
                        isTopShown.set(true);
                    }
                } else if (newState.equals(PanelState.COLLAPSED))
                {
                    if (isTopShown.get()) switcher.showNext();
                    isTopShown.set(false);
                }
            }
        });
        mLayout.setFadeOnClickListener(view -> mLayout.setPanelState(PanelState.COLLAPSED));


        //----------------------------------Player control buttons----------------------------------
        //Stop button
        ImageButton stopPlayback = findViewById(R.id.button_stop);
        stopPlayback.setOnClickListener(v -> playerStop());

        // Start button
        ImageButton startPlayback = findViewById(R.id.button_play);
        startPlayback.setOnClickListener(v -> MyThreadPool.INSTANCE.getExecutorService().execute(this::playerStart));

        //Pause button
        ImageButton pausePlayback = findViewById(R.id.button_pause);
        pausePlayback.setOnClickListener(v -> playerPause());

        //Toogle play-stop button
        ImageView toggleButton = findViewById(R.id.button_play_stop);
        toggleButton.setOnClickListener(v -> {

            if (cookieRefs.get() == null)
            {
                Toast.makeText(this, STATION_NOT_SELECTED, Toast.LENGTH_LONG).show();
                return;
            }

            if (isPlaying.get())
                playerStop();
            else
                MyThreadPool.INSTANCE.getExecutorService().execute(this::playerStart);
        });

        //set progress ring
        progressBar = (findViewById(R.id.progress_circle));
//        progressBar.setProgressDrawable(getDrawable(R.drawable.circular));

        //Add Home screen
        addHomeScreenFragment();
        playbackManager = new PlaybackManager(MainActivity.this);

//        HistoryManager.initHistory();
//        PlaylistManager.initPlaylist();

        // Create wakelock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "muze_radio: wakelock");
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "myId");

        wakelock.acquire(10 * 60 * 1000L /*10 minutes*/);
        wifiLock.acquire();

        initBroadcastReceiver();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        handleSharedStation(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleSharedStation(getIntent());

//            Bundle bundle = intent.getExtras();
//            if (bundle != null)
//            {
//                  handleSharedStation(getIntent().getParcelableExtra(Intent.EXTRA_INTENT));
//                MyPrint.printOut("Main", "Bundle is not null, String is: " +  bundle.getString("from_notifier"));
//            }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_about:
            {
                if (mLayout != null)
                {
                    addAboutFragment();
                    return true;
                }
            }
            case R.id.menu_settings:
            {
                addPrefsFragment();
                return true;
            }
            case R.id.menu_equalizer:
            {
                addEqFragment();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED))
        {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else
        {
            super.onBackPressed();
//            finish();
        }
    }

    @Override
    public void slidePanel(int direction)
    {
        MyHandler.getHandler().post(() ->
        {
            if (direction == PANEL_SLIDE_UP)
                mLayout.setPanelState(PanelState.EXPANDED);
            else
                mLayout.setPanelState(PanelState.COLLAPSED);
        });
    }

    // Pop up favorites
    public void createPopup()
    {
        final ImageButton btnPopup = findViewById(R.id.button_overflow);
        btnPopup.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, btnPopup);
            popup.getMenuInflater().inflate(R.menu.menu_player, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_favorites)
                {
                    addToFavorites();
                    Toast.makeText(MainActivity.this, getResources()
                                    .getString(R.string.added_to_favorites) + " " +
                                    cookieRefs.get().getTitle(), Toast.LENGTH_SHORT)
                            .show();
                }
                if (item.getItemId() == R.id.radio_detail)
                {
                    MyThreadPool.INSTANCE.getExecutorService().execute(MainActivity.this::displayRadioDetail);
                }
                if (item.getItemId() == R.id.radio_share)
                {
                    MyThreadPool.INSTANCE.getExecutorService().execute(MainActivity.this::share);
                }

                return true;
            });

            popup.show(); //showing popup menu
        }); //closing the setOnClickListener method
    }

    //    -----------------------------------------/ Player functions /----------------------------------
    public synchronized void playerStart()
    {
        if (playbackManager == null) return;

        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }
        if (!playbackManager.paused())
            updatePlaybackStatus(MSG_CONNECTING);

        isPlaying.set(true);
        setToggleButton();

        playbackManager.startPlayingStation();

        setPlayButtonEnabled(false);
        setPausedButtonEnabled(true);
        setStopButtonEnabled(true); //change
    }

    private void playerStop()
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                playbackManager.stopPlayingStation());
    }

    private void playerPause()
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
            if (playbackManager != null && isPlaying.get())
            {
                playbackManager.pausePlayingStation();
                setPlayButtonEnabled(true);
                setPausedButtonEnabled(false);
                setStopButtonEnabled(true); //change
            }
        });
    }

    private synchronized void resetTransport()
    {
//         MyPrint.printOut(TAG, "resetTransport");
        progressBar.setProgress(MAX); // resets the ring
        isPlaying.set(false);
        setToggleButton();
        setPlayButtonEnabled(true);
        setPausedButtonEnabled(false); //change
        setStopButtonEnabled(false); //change
        playbackManager.setPaused(false);
    }

    private void setPlayButtonEnabled(boolean enabled)
    {
        final ImageButton btnPlay = findViewById(R.id.button_play);
        MyHandler.getHandler().post(() -> {
            btnPlay.setEnabled(enabled);
//                Drawable img = getDrawable(R.drawable.ic_play_new).mutate();
            if (enabled)
                btnPlay.setImageAlpha(0xFF);
            else
//                    btnPlay.setImageAlpha(0x3F);
                btnPlay.setImageAlpha(BUTTON_OPACITY);
        });
    }

    private void setPausedButtonEnabled(boolean enabled)
    {
        // MyPrint.printOut(TAG, "setPausedButtonEnabled: " + enabled);
        final ImageButton pause = findViewById(R.id.button_pause);

        MyHandler.getHandler().post(() -> {
            pause.setEnabled(enabled);
            if (enabled)
                pause.setImageAlpha(0xFF);
            else
                pause.setImageAlpha(BUTTON_OPACITY);
        });
    }

    private void setStopButtonEnabled(boolean enabled)
    {
        final ImageButton stop = findViewById(R.id.button_stop);
        MyHandler.getHandler().post(() -> {
            stop.setEnabled(enabled);
//                Drawable img = getDrawable(R.drawable.ic_stop_new).mutate();
            if (enabled)
                stop.setImageAlpha(0xFF);
            else
                stop.setImageAlpha(BUTTON_OPACITY);
        });
    }

    //Entry to player session
    public void startPlayerSession(final String url)
    {
        if (url != null)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
                try
                {
                    if (playbackManager != null)
                    {
                        playbackManager.stopPlayingStation();
//                            playbackManager = null;
                        playbackManager.setUrl(url);
                        playerStart();
                    }
//                        playbackManager = new PlaybackManager(MainActivity.this);

                } catch (Exception e)
                {
                    ExceptionHandler.onException(MainActivity.class.getSimpleName(), e);
                }
            });
        } else
        {
            updatePlaybackStatus(MSG_FAIL_CONNECT);
        }
    }

    public void onStoppingSession(final String message)
    {
        updatePlaybackStatus(message);
        resetTransport();

        if (message.equalsIgnoreCase(ConnectionClient.msgStationUnavail))
            CoverArtClient.getInstance()
                    .setController(MainActivity.this)
                    .showDefaultLogo();
    }


    public void onPlaybackStarted()
    {
        // MyPrint.printOut(TAG, "onPlaybackStarted");
        if (playbackManager.paused()) return;

        MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
            addToRecent();
            CoverArtClient.getInstance()
                    .setController(MainActivity.this)
                    .cancelCallWithTag(CoverArtClient.TAG_DEFAULT_LOGO)
                    .showDefaultLogo();
        });
    }


    private void setToggleButton()
    {
        final ImageButton btnToggle = findViewById(R.id.button_play_stop);

        MyHandler.getHandler().post(() -> {
            synchronized (lock2)
            {
                if (isPlaying.get())
                {
                    btnToggle.setImageResource(R.drawable.ic_stop_black_24dp);
                } else
                {
                    btnToggle.setImageResource(R.drawable.ic_play_new);
                }
            }
        });
    }

    public StationCookie getCookieRefs() //TODO: problem sync
    {
        return cookieRefs.get();
    }

    //Entry: Received from Station Fragment to start playback
    @Override
    public void onStationInfoAvailable(@Nullable final StationCookie station)
    {
        if (station == null) return;

        startPlayerSession(station.getUrlResolved());
        cookieRefs.set(station);
        updateRadioInfo();
    }

    //adds Radio info to the MainActivity text fields; called from PlaybackManager
    public void updateRadioInfo()
    {
        TextView lblStationName = findViewById(R.id.station_name); // main viewer
        TextView lblStationTags = findViewById(R.id.station_tags);
        TextView lblStationCountry = findViewById(R.id.station_country);
        TextView lblRadioName = findViewById(R.id.label_radio); // bottom panel

        String title = getCookieRefs().getTitle();
        String genre = getCookieRefs().getGenre();
        String country = getCookieRefs().getCountry();

        MyHandler.getHandler().post(() -> {
            lblRadioName.setText(title);
            lblStationName.setText(title);
            lblStationCountry.setText(country);
            lblStationTags.setText(StringCapitalizer.capitalize(genre));
        });
    }

    public void updateLastPlayedStationDefaultName(String title)
    {
        TextView lblRadioName = findViewById(R.id.label_radio);

        MyHandler.getHandler().post(() -> {
            if (title != null && !title.isEmpty())
            {
                lblRadioName.setText(title);
            }
        });
    }

    public void updateLastPlayedStation()
    {
        if (!isPlaying.get())
            CoverArtClient.getInstance()
                    .setController(MainActivity.this)
                    .showDefaultLogo();

//            CoverArtManager.getInstance(MainActivity.this).showStationLogo(cookieRefs.get().getThumbUrl());

        MyThreadPool.INSTANCE.getExecutorService().execute(this::updateRadioInfo);
    }

    @Subscribe
    public void onBufferingEvent(final Events.BufferingEvent event)
    {
        MyHandler.getHandler().post(() -> progressBar.setProgress(event.increment * 10));

        String status = STRING_BUFFERING + event.increment * 10 + "%";

        if (event.increment >= 10)
            status = STRING_EMPTY;

        updatePlaybackStatus(status);
    }

    @Subscribe
    public void onFailureEvent(Events.FailEvent event)
    {
        onStoppingSession(event.message);
    }

    private void addToFavorites()
    {
//        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
//                FavoritesManager.getInstance()
//                        .doAccessDB(cookieRefs.get(), FavoritesManager.Perform.INSERT));

//        MyThreadPool.INSTANCE.getExecutorService().execute(() -> Repository.getInstance()
//                        .insertFavoriteStation(cookieRefs.get()));


        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                Repository.getInstance().insertFavoriteStation(cookieRefs.get()));

    }

    // add to history
    private void addToRecent()
    {
//        MyThreadPool.INSTANCE.getExecutorService().execute(() -> HistoryManager.addToRecent(cookieRefs.get()));
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                Repository.getInstance().insertHistoryStation(cookieRefs.get()));
    }


    //-----------------------------------------------/ Update UI Methods /--------------------------
    public void updateArtistSongName(final String data)
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() -> {

            final TextView lblSongName = findViewById(R.id.label_song);
            final TextView lblTopPanel = findViewById(R.id.artist_name_top);

            MyHandler.getHandler().post(() -> {
                lblSongName.setText(data);
                lblTopPanel.setText(data);
                lblSongName.setSelected(true);
            });
        });

//                    lblSongName.setSelected(true);
//                    lblSongName.setEllipsize(TextUtils.TruncateAt.END);
//                    lblSongName.setSingleLine(true);
    }

    //updates bottom panel text view with Playback or Connection events (stopped, failed, playing, etc.)
    public void updatePlaybackStatus(final String status)
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
            synchronized (lock1)
            {
                TextView lblSongName = findViewById(R.id.label_song);
                TextView lblTopPanel = findViewById(R.id.artist_name_top);
                MyHandler.getHandler().post(() ->
                {
                    if (lblSongName != null)
                        lblSongName.setText(status);

                    if (lblTopPanel != null)
                        lblTopPanel.setText(STRING_EMPTY);
                });
            }
        });
    }

    public void displayCoverArt(final String artData)
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() -> CoverArtClient.getInstance()
                .setController(MainActivity.this)
                .cancelCallWithTag(CoverArtClient.TAG_COVER_ART)
                .fetchCoverArt(artData));

//            onCoverArtAvailable(image);
//            ArtModel.getInstance(this).getCoverArt(artData);
    }

    public void displayRadioDetail()
    {

        StationCookie cookie = cookieRefs.get();

        if (cookie == null) return;

        String strBitRate;
        ArrayList<String> list = new ArrayList<>();

        list.add(RadioDetailFragment.RadioInfo.STATION, cookie.getTitle());
        list.add(RadioDetailFragment.RadioInfo.GENRE, cookie.getGenre());
        list.add(RadioDetailFragment.RadioInfo.COUNTRY, cookie.getCountry());
        list.add(RadioDetailFragment.RadioInfo.LANG, cookie.getLanguage());
        list.add(RadioDetailFragment.RadioInfo.HOMEPAGE, cookie.getHomepage());
        list.add(RadioDetailFragment.RadioInfo.URL_THUMB, cookie.getThumbUrl());

        if (cookie.getBitrate() < 32)
            strBitRate = StationCookie.NOT_AVAIL_STRING;
        else
            strBitRate = String.valueOf(cookie.getBitrate());

        list.add(RadioDetailFragment.RadioInfo.BITRATE, strBitRate);
        list.add(RadioDetailFragment.RadioInfo.CODEC, cookie.getCodec());

        list.add(cookie.getThumbUrl());
        addRadioDetailFragment(list);

        MyHandler.getHandler().post(() ->
                mLayout.setPanelState(PanelState.COLLAPSED));
    }

    @Override
    public void setBackButton(boolean set)
    {
        // MyPrint.printOut(TAG, "setBackButton: " + set);

        Toolbar navbar = findViewById(R.id.main_toolbar);

        if (set)
        {
            navbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
            navbar.setNavigationOnClickListener(view -> onBackPressed());
        } else
        {
            navbar.setNavigationIcon(R.drawable.ic_home_black_24dp);
            navbar.setNavigationOnClickListener(null);
        }
    }

    @Override
    public void updateAppBarTitle(String title, String subtitle, boolean enable)
    {
        ActionBar bar = getSupportActionBar();

        if (bar == null) return;

        if (subtitle != null)
        {
            bar.setTitle(title);
            bar.setSubtitle(subtitle);
        } else
        {
            bar.setTitle(title);
            bar.setSubtitle(null);
        }
        setBackButton(enable);
    }


    //    -----------------------------------------/ Controller Methods /----------------------------------
    //displays Home screen
    @Override
    public void addHomeScreenFragment()
    {
        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag(HOME);

        if (homeFragment == null)
            homeFragment = HomeFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_place, homeFragment)
                .commit();
    }

    //displays SubGenres screen
    @Override
    public void addGenreScreenFragment(int selection)
    {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(GENRE);
        if (frag == null)
            frag = GenreFragment.newInstance(selection);

        replaceFragment(frag);
    }

    //displays Stations screen
    @Override
    public void addStationScreenFragment(String selection, int flag)
    {
        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentByTag(STATION);
        if (frag == null)
            frag = StationsFragment.newInstance(selection, flag);

        replaceFragment(frag);
    }

    //displays Stations screen for countries
    @Override
    public void addStationScreenFragment(String selection, String countryCode, int flag)
    {
        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentByTag(STATION);

        if (frag == null)
        {
            frag = StationsFragment.newInstance(selection, countryCode, flag);
        }

        replaceFragment(frag);
    }
    @Override
    public void addFavsScreenFragment()
    {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(FAVORITES);
        if (frag == null)
            frag = FavsFragment.newInstance();

        replaceFragment(frag);
    }

    @Override
    public void addPlaylistFragment()
    {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PLAYLIST);
        if (frag == null)
            frag = PlaylistFragment.newInstance();

        replaceFragment(frag);
    }

    @Override
    public void addCountriesScreenFragment()
    {
        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentByTag(COUNTRY);

        if (frag == null)
            frag = CountriesFragment.newInstance();

        replaceFragment(frag);
    }

    @Override
    public void addLangsFragment()
    {
        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentByTag(LANGS);

        if (frag == null)
            frag = LanguageFragment.newInstance();

        replaceFragment(frag);
    }

    @Override
    public void addHistoryFragment()
    {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(HISTORY);

        if (frag == null)
            frag = HistoryFragment.newInstance();

        replaceFragment(frag);
    }

    public void addPrefsFragment()
    {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PREFS);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);

        if (frag == null)
            frag = PrefsFragment.newInstance();

        if (currentFragment != null && currentFragment.getClass().equals(frag.getClass()))
            return;

        addFragment(frag);
    }

    public void addRadioDetailFragment(ArrayList<String> radioDetail)
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);
        Fragment frag = getSupportFragmentManager().findFragmentByTag(RADIO_DETAIL);

        if (frag == null)
            frag = RadioDetailFragment.newInstance(radioDetail);

        if (currentFragment != null && currentFragment.getClass().equals(frag.getClass()))
            return;

        replaceFragment(frag);
    }

    public void addEqFragment()
    {
        EqualizerManager eqManager = EqualizerManager.getInstance();

        if (eqManager.getCurrentSessionID() <= 0)
        {
            MyHandler.getHandler().post(() -> Toast.makeText(getApplicationContext(), "Equalizer available on playback", Toast.LENGTH_SHORT).show());
            return;
        }

        Fragment frag = EqualizerManager.getInstance().getEqualizerFragment();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);

        if (currentFragment != null && currentFragment.getClass().equals(frag.getClass()))
            return;

        addFragment(frag);
    }

    public void addAboutFragment()
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_place);
        Fragment frag = getSupportFragmentManager().findFragmentByTag(ABOUT);

        if (frag == null)
            frag = AboutFragment.newInstance();

        if (currentFragment != null && currentFragment.getClass().equals(frag.getClass()))
            return;

        addFragment(frag);
    }

    @Override
    public void addSearchFragment()
    {
        if (isDeviceConnected())
        {
            notifyDeviceNotConnected();
            return;
        }

        Fragment frag = getSupportFragmentManager().findFragmentByTag(SEARCH);
        if (frag == null)
            frag = SearchFragment.newInstance();

        replaceFragment(frag);
    }


    private void replaceFragment(Fragment frag)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_place, frag)
                .addToBackStack(null)
                .commit();
    }

    private void addFragment(Fragment frag)
    {
        FragmentManager fragManager = getSupportFragmentManager();
        fragManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragManager.beginTransaction()
                .add(R.id.fragment_place, frag)
                .addToBackStack(BACK_STACK_ROOT_TAG)
                .commit();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean panelMode = prefs.getBoolean("station_panel_mode", true);

        if (panelMode && PanelState.COLLAPSED.equals(mLayout.getPanelState()) && isPlaying.get())
            slidePanel(MainActivity.PANEL_SLIDE_UP);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(playbackManager))
            EventBus.getDefault().register(playbackManager);

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        AppSetup.loadData();

        cookieRefs.set(AppSetup.getLastPlayedCookie());

        if (Objects.nonNull(cookieRefs.get()))
        {
            updateLastPlayedStation();
            playbackManager.setUrl(cookieRefs.get().getUrlResolved());
        } else
        {
            updateLastPlayedStationDefaultName(AppSetup.DEFAULT);
        }


        IntentFilter filter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
        filter.setPriority(1000);

    }

    @Override
    protected void onStop()
    {
        super.onStop();

      updataDatabaseOnExit();

        // ger Preferences object
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences prefs = getSharedPreferences(AppSetup.SETTINGS, Context.MODE_PRIVATE);
        //show last played station
        boolean lastPlayed = prefs.getBoolean("last_played", true);
        if (lastPlayed)
            AppSetup.setLastPlayedCookie(cookieRefs.get());
        else
            AppSetup.setLastPlayedCookie(null);

        // get current EQ settings
        EqualizerSettings settings = EqualizerManager.getInstance().getCurrentSettings();
        if (settings != null)
            AppSetup.setEQsettings(settings);

        // save all data
        AppSetup.saveAll();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //shutdown playback service
        if (playbackManager != null)
        {
            playbackManager.killPlayback();
            playbackManager.shutdownService();
            playbackManager = null;
        }

        if (EventBus.getDefault().isRegistered(playbackManager))
            EventBus.getDefault().unregister(playbackManager);

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        //unregister notification broadcast
        unregisterReceiver(notificationReceiver);
        //release locks
        wakelock.release();
        wifiLock.release();

        MyThreadPool.INSTANCE.getExecutorService().shutdown();
    }

     private synchronized void updataDatabaseOnExit()
    {
//        PlaylistManager.updatePlaylistDB();
//        HistoryManager.updateHistoryDB();
    }

    private void notifyDeviceNotConnected()
    {
        MyHandler.getHandler().post(() ->
                Toast.makeText(getApplicationContext(), "Device is not connected", Toast.LENGTH_LONG).show());
    }

    public boolean isDeviceConnected()
    {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    public void initBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackNotifier.ACTION.PLAY_ACTION);
        filter.addAction(PlaybackNotifier.ACTION.STOP_ACTION);
        filter.addAction(PlaybackNotifier.ACTION.CLOSE_ACTION);

        notificationReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent == null || intent.getAction() == null) return;

                switch (intent.getAction())
                {
                    case PlaybackNotifier.ACTION.PLAY_ACTION:
                        if (!isPlaying.get())
                        {
                            MyThreadPool.INSTANCE.getExecutorService().execute(() -> playerStart());
                        }
                        break;
                    case PlaybackNotifier.ACTION.STOP_ACTION:
                        if (isPlaying.get())
                            playerStop();
                        break;
                    case PlaybackNotifier.ACTION.CLOSE_ACTION:
                        finishAndRemoveTask();
                        break;
                }
            }
        };
        registerReceiver(notificationReceiver, filter);
    }

//-----------------------------------------------------Intent--------------------------------------//

    public void handleSharedStation(Intent intent)
    {
        MyPrint.printOut("Intent", "received");

        if (!Objects.isNull(intent))
        {
            Uri uri = intent.getData();

            if (uri != null)
            {
                String station = uri.getQueryParameter("share");
                MyPrint.printOut("Intent address", station);
                addStationScreenFragment(station, GenreFlags.SINGLE_STATION);

                if (mLayout.getPanelState() == PanelState.EXPANDED)
                {
                    slidePanel(MainActivity.PANEL_SLIDE_DOWN);
                }
            }
        } else
        {
            MyPrint.printOut("Intent is", "null");
        }
    }

//    private static final String STATION_DATA = "com.radio.share_station_data";
//    private static final String STATION_SHARING = "muzeradio_share_station";

    public void share()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
//        String address = "http://open.muzeradio/station?share=" + cookieRefs.get().getUuid();

        String address = "https://muze-radio.tiiny.site?share=" + cookieRefs.get().getUuid();

        intent.putExtra(Intent.EXTRA_SUBJECT, cookieRefs.get().getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, address);
//        intent.putExtra(STATION_SHARING, STATION_DATA);

        startActivity(Intent.createChooser(intent, "Share via:"));
    }

    public void onMediaButtonClick()
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());

        if (prefs.getBoolean("bluetooth_media_button", true))
        {
            if (isPlaying.compareAndSet(true, false))
            {
                MainActivity.this.playerStop();
            } else
            {
                MainActivity.this.playerStart();
                isPlaying.set(true);
            }
        }
    }
}








