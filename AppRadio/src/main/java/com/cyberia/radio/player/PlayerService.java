package com.cyberia.radio.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.NonNull;
import androidx.media.session.MediaButtonReceiver;
import com.cyberia.radio.equalizer.EqualizerManager;
import com.cyberia.radio.eventbus.Events.FailEvent;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.io.ConnectionClient;
import com.cyberia.radio.io.MyInputStreamDataSource;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.Objects;

public class PlayerService extends Service
{
    private ExoPlayer player;
    private PlaybackManager controller;
    private MediaSessionCompat mediaSession;

    private volatile MediaButtonHandler mediaButtonHandler;
    private final Context context;


    public PlayerService()
    {
        context = MyAppContext.INSTANCE.getAppContext();
    }

    @Override
    public void onCreate()
    {
        createMediaSession();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startPlayback();

//        MediaButtonReceiver.handleIntent(null, intent);
//        return super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }


    public void setController(PlaybackManager manager)
    {
        controller = manager;
        mediaButtonHandler.addMediaEvenListener(manager);
    }

    public void removeController()
    {
        controller = null;
    }

    public boolean isPlaying()
    {
        return player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady();
    }

    public void stopPlayer()
    {
        MyHandler.post(() -> {
            if (player != null && isPlaying())
            {
                player.stop();
            }
        });

        EqualizerManager.getInstance().removeFromSession();
    }

    public void terminatePlayer()
    {
        if (!Objects.isNull(player))
        {
            player.stop();
            player.release();
            EqualizerManager.getInstance().removeFromSession();
        }
    }

    public void stopPlayback()
    {
        stopPlayer();

        if (controller != null)
            controller.onPlayerStopped();

//        EqualizerManager.getInstance().removeFromSession();
    }

    public void startPlayback()
    {
        MyHandler.getHandler().post(() -> {
            if (player != null)
                player.setPlayWhenReady(true);

            if (controller != null)
                controller.onPlayerStart();
        });
    }

    public void pausePlayback()
    {
        MyHandler.getHandler().post(() -> {
            if (player != null)
                player.setPlayWhenReady(false);
        });
    }

    public void initPlayer(InputStream is, Uri uri)
    {
        stopPlayer();

        MyHandler.getHandler().post(() -> PlayerService.this.prepareExoPlayer(is, uri));
    }

    public void prepareExoPlayer(InputStream is, Uri uri)
    {
        if (Objects.isNull(player))
        {
            final DefaultLoadControl loadControl = new DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(2000, 5000, 1000, 2000)
                    .build();

            player = new ExoPlayer.Builder(this)
                    .setLoadControl(loadControl)
                    .build();

            player.setHandleAudioBecomingNoisy(true);

            MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);

            mediaSessionConnector.setQueueNavigator((new TimelineQueueNavigator(mediaSession)
            {
                @NonNull
                @Override
                public MediaDescriptionCompat getMediaDescription(@NonNull Player player, int windowIndex)
                {
                    Bundle extras = new Bundle();
                    extras.putInt(MediaMetadataCompat.METADATA_KEY_DURATION, -1);

                    return new MediaDescriptionCompat.Builder()
                            .setExtras(extras)
                            .build();
                }
            }));

            mediaSessionConnector.setMediaButtonEventHandler(mediaButtonHandler);
            mediaSessionConnector.setPlayer(player);

            onPlayerEvents();
        }


        final MyInputStreamDataSource myDataSource = new MyInputStreamDataSource(new DataSpec(uri), is);
        final DataSource.Factory factory = () -> myDataSource;

        final MediaSource audioSource = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(MediaItem.fromUri(uri));

        player.setMediaSource(audioSource);

        player.prepare();
    }

    void killPlayer()
    {
        if (Objects.nonNull(player))
        {
            player.release();
            player = null;
        }
    }

    private void createMediaSession()
    {
        if (mediaSession == null)
        {
            ComponentName componentName = new ComponentName(context, MediaButtonReceiver.class);

            if (!mediaButtonEnabled(componentName))
            {
                context.getPackageManager().setComponentEnabledSetting
                        (componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(componentName);
            PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            mediaSession = new MediaSessionCompat(context, "tag_session_MuzeRadio", componentName, mediaButtonReceiverPendingIntent);
            mediaSession.setActive(true);
        }

        if (mediaButtonHandler == null)
            mediaButtonHandler = new MediaButtonHandler();
    }

    private boolean mediaButtonEnabled(ComponentName componentName)
    {
        int status = context.getPackageManager().getComponentEnabledSetting(componentName);

        return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || status == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }


    /*----------------------------------Service and Notification Methods ----------------------------*/
    private final MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return myBinder;
    }

    public class MyBinder extends Binder
    {
        public PlayerService getService()
        {
            return PlayerService.this;
        }
    }

    public synchronized void notifyActivity(final String station, final String thumbUrl)
    {
        PlaybackNotifier notifier = PlaybackNotifier.getInstance();
        notifier.setMediaSession(mediaSession);
        startForeground(PlaybackNotifier.NOTIFICATION_ID, notifier.getNotification());
        notifier.updateNotificationStation(station, thumbUrl);
    }

    public synchronized void updateNotification(String songTitle)
    {
        PlaybackNotifier.getInstance().updateNotificationSong(songTitle);
    }

    public void removeNotification()
    {
        PlaybackNotifier.getInstance().removeNotification(this);
    }


    private void onPlayerEvents()
    {
        player.addAnalyticsListener(new AnalyticsListener()
        {
            @Override
            public void onPlaybackStateChanged(@NonNull EventTime eventTime, int playbackState)
            {
                if (playbackState == Player.STATE_READY)
                {
                    int sessionID = player.getAudioSessionId();
                   MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                           EqualizerManager.getInstance().initEqualizer(sessionID));
                }
            }

            @Override
            public void onPlayerError(@NonNull EventTime eventTime, @NonNull PlaybackException error)
            {
                MyPrint.print("Exoplayer", "Player error; error code: " + error.errorCode, 319);
                EventBus.getDefault().post(new FailEvent(ConnectionClient.msgConnLost));
            }

            @Override
            public void onAudioUnderrun(@NonNull EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs)
            {
                MyPrint.printOut("Exoplayer", "Audio underflow");
            }
        });
    }

}
