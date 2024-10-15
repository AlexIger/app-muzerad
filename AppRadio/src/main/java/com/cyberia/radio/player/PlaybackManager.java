package com.cyberia.radio.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.eventbus.Events;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.io.ConnectionClient;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class PlaybackManager implements ConnectionClient.ConnectionCallback, ServiceConnection, MediaButtonHandler.BTActionEventListener
{
    final static String TAG = "PlaybackManager";
    final static String NO_STATION_URL = "Station address not available";
    private final AtomicReference<String> radioUrl = new AtomicReference<>();
    private ConnectionClient connectionClient;
    private PlayerService player;
    private final MainActivity controller;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private Intent serviceIntent;

    public PlaybackManager(MainActivity con)
    {
        controller = con;
        bindService();
    }


    public void bindService()
    {
        //start the service
        serviceIntent = new Intent(controller, PlayerService.class);
        controller.startForegroundService(serviceIntent);
        controller.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    public synchronized void setUrl(String url)
    {
        radioUrl.set(url);
    }

    public synchronized void initConnection()
    {
        connectionClient = ConnectionClient.newInstance(this);
        connectionClient.startSession(radioUrl.get());
    }

    public MainActivity getController()
    {
        return controller;
    }

    void resumePlayback()
    {
        if (player != null) player.startPlayback();
    }

    //initiate a connection to the station's url
    public void startPlayingStation()
    {
        if (isPaused.get())
        {
            resumePlayback();
            isPaused.set(false);
        } else
        {
            if (!TextUtils.isEmpty(radioUrl.get()))
            {
                initConnection();
            } else
            {
                controller.runOnUiThread(() -> Toast.makeText(controller.getApplicationContext(), NO_STATION_URL, Toast.LENGTH_LONG).show());
            }
        }
    }

    public void stopPlayingStation()
    {
        if (player != null)
            player.stopPlayback();

        disconnect();
    }

    public void killPlayback()
    {
        MyPrint.printOut("Player Manager", "Kill the app");
        if (player != null)
            player.terminatePlayer();

        disconnect();
    }

    private void disconnect()
    {
        try
        {
            if (connectionClient != null)
            {
                connectionClient.cancelCall();
                connectionClient.stopSession();
            }

        } catch (Exception e)
        {
            ExceptionHandler.onException(TAG, e);
        }
    }

    public void pausePlayingStation()
    {
        if (player != null)
        {
            player.pausePlayback();
            isPaused.set(true);
        }
    }

    public boolean paused()
    {
        return isPaused.get();
    }

    public void setPaused(boolean bool)
    {
        isPaused.set(bool);
    }

    @Override
    public void onConnectionEstablished()
    {
        // create a notification
       MyThreadPool.INSTANCE.getExecutorService().execute((() -> player.notifyActivity(controller.getCookieRefs().getTitle(), controller.getCookieRefs().getThumbUrl())));
    }

    public void updateNotification(String artistSong)
    {
       MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
           if (player != null)
               player.updateNotification(artistSong); //changes
       });
    }

    // called from ConnectionManager when the buffered stream is ready
    @Override
    public synchronized void onStreamAvailable(InputStream stream)
    {
        if (player != null)
        {
            try
            {
                Intent intent = new Intent(controller, PlayerService.class);
                player.initPlayer(stream, Uri.parse(radioUrl.get()));
                controller.startForegroundService(intent);
            } catch (Exception e)
            {
                ExceptionHandler.onException(TAG, e);
            }
        }
    }

    public synchronized void onPlayerStart()
    {
        controller.onPlaybackStarted();
    }

    public synchronized void onPlayerStopped()
    {
        controller.onStoppingSession(MainActivity.MSG_PLAYBACK_STOP);
    }

    //callback from MetaDataManager
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSongDataAvailableEvent(Events.SongMetadataEvent data)
    {
//        MyPrint.print(TAG, "Event received from EvBus: " + data.metadata, 215);

        updateNotification(data.metadata);
        controller.updateArtistSongName(data.metadata);
        controller.displayCoverArt(data.metadata);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        player = ((PlayerService.MyBinder)service).getService();
        player.setController(PlaybackManager.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        MyPrint.printOut(TAG, "onServiceDisConnected");
        player = null;
    }

    public void shutdownService()
    {
        if (player != null)
        {
            player.killPlayer();
            player.removeNotification();
            player.removeController();
            player.stopService(serviceIntent);
            player = null;

            controller.unbindService(this);
        }
    }

    @Override
    public void onMediaButtonSingleClick()
    {
        controller.onMediaButtonClick();
    }
}
