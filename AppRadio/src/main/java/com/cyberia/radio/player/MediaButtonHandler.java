package com.cyberia.radio.player;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

//import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

import java.util.Objects;

public class MediaButtonHandler implements MediaSessionConnector.MediaButtonEventHandler
{
    public interface BTActionEventListener
    {
        void onMediaButtonSingleClick();
    }

    private BTActionEventListener btnListener;

    public void addMediaEvenListener(BTActionEventListener listener)
    {
        btnListener = listener;
    }

    @Override
    public boolean onMediaButtonEvent(@NonNull Player player,  @NonNull Intent mediaButtonEvent)
    {
        if (Objects.nonNull(player) && Objects.nonNull(btnListener) && Objects.nonNull(mediaButtonEvent)
                && Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonEvent.getAction()))
        {
            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null && keyEvent.getAction() != KeyEvent.ACTION_DOWN)
            {
                btnListener.onMediaButtonSingleClick();
            }
        }

        return true;
    }

}
