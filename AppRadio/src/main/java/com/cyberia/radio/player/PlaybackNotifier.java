package com.cyberia.radio.player;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSession;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.MainActivity;
import com.cyberia.radio.R;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.io.ConnectionClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaybackNotifier
{
    public interface ACTION
    {
        String MAIN_ACTION = "com.radio.action.main";
        String PLAY_ACTION = "com.radio.action.play";
        String STOP_ACTION = "com.radio.action.stop";
        String CLOSE_ACTION = "com.radio.action.close";
        String FROM_PLAYBACK_NOTIFIER = "from_notifier";
    }

    private static final String CHANNEL_ID = "MuzeRadioPlayerService";
    private static final String CHANNEL_NAME = "Muze Radio";
    private volatile NotificationCompat.Builder notificationBuilder;
    public final static int NOTIFICATION_ID = 1001;

    private volatile Notification notification;
    private static volatile PlaybackNotifier instance;
    private MediaSessionCompat mediaSession;
    private final Context con = MyAppContext.INSTANCE.getAppContext();


    public PlaybackNotifier()
    {
    }

    public static PlaybackNotifier getInstance()
    {
        if (instance == null)
        {
            instance = new PlaybackNotifier();
        }

        return instance;
    }

    public void setMediaSession(MediaSessionCompat session)
    {
        mediaSession = session;
    }

    public Notification getNotification()
    {
        if (notification == null)
            notification = buildNotification();

        return notification;
    }

    private Notification buildNotification()
    {
        createNotificationChannel();

        Intent notificationIntent = new Intent(con, MainActivity.class);
        notificationIntent.setAction(ACTION.MAIN_ACTION);
        notificationIntent.putExtra(ACTION.FROM_PLAYBACK_NOTIFIER, ACTION.MAIN_ACTION);
        PendingIntent openMainActivity = PendingIntent.getActivity(con, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent intentPlay = new Intent();
        intentPlay.setAction(ACTION.PLAY_ACTION);
        PendingIntent playIntent = PendingIntent.getBroadcast(con, 0, intentPlay, PendingIntent.FLAG_IMMUTABLE);

        //Intent for Close
        Intent intentClose = new Intent();
        intentClose.setAction(ACTION.CLOSE_ACTION);
        PendingIntent closeIntent = PendingIntent.getBroadcast(con, 0, intentClose, PendingIntent.FLAG_IMMUTABLE);

        //Intent for Stop
        Intent intentStop = new Intent();
        intentStop.setAction(ACTION.STOP_ACTION);
        PendingIntent stopIntent = PendingIntent.getBroadcast(con, 0, intentStop, PendingIntent.FLAG_IMMUTABLE);

        //Media Buttons
        NotificationCompat.Action play = new NotificationCompat.Action.Builder(R.drawable.ic_notif_play, "Play", playIntent).build();
        NotificationCompat.Action stop = new NotificationCompat.Action.Builder(R.drawable.ic_notif_stop, "Stop", stopIntent).build();
        NotificationCompat.Action close = new NotificationCompat.Action.Builder(R.drawable.ic_notif_close, "Close", closeIntent).build();

        MediaStyle style = new MediaStyle()
                .setShowActionsInCompactView(0, 1, 2);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            style = new MediaStyle().setMediaSession(mediaSession.getSessionToken());

        notificationBuilder = new NotificationCompat.Builder(con, CHANNEL_ID);

        return notificationBuilder
                .setSmallIcon(R.drawable.ic_notif_music)
                .setContentText(MainActivity.STRING_EMPTY)
                .setContentIntent(openMainActivity)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .addAction(play) //you can set a specific icon
                .addAction(stop)
                .addAction(close)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(style)
                .build();
    }


    public void updateNotificationStation(final String station, final String url)
    {
        Drawable drawable = AppCompatResources.getDrawable
                (con, !AppRadio.isNightMode ? R.drawable.ic_notif_no_thumb : R.drawable.ic_notif_no_thumb_blk);

        Bitmap defaultIcon = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(defaultIcon);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        //Get the notification object
        NotificationManager manager = (NotificationManager) getSystemService(con, NotificationManager.class);

        notificationBuilder.setContentTitle(station);

        if (!TextUtils.isEmpty(url))
        {
            OkHttpClient client = ConnectionClient.OkSingleton.getInstance().getClient();

            client.newBuilder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(HttpUrl.parse(url))
                    .build();

            Call call = client.newCall(request);

            try (Response response = call.execute())
            {
                if (response.isSuccessful())
                {
                    if (response.body() == null)
                        throw new IOException("Response doesn't contain a file");

                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    notificationBuilder.setLargeIcon(bitmap);
                }
            }
            catch (IOException e)
            {
                ExceptionHandler.onException("PlaybackNotifier", e);
                notificationBuilder.setLargeIcon(defaultIcon);
                manager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        }
        else
        {
            notificationBuilder.setLargeIcon(defaultIcon);
        }

        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void updateNotificationSong(final String songTitle)
    {
        notificationBuilder.setContentText(songTitle);

        Context con = MyAppContext.INSTANCE.getAppContext();
        NotificationManager manager = con.getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel()
    {
        Context con = MyAppContext.INSTANCE.getAppContext();

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = con.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    public void removeNotification(Service service)
    {
        service.stopForeground(true);
    }

}
