package com.cyberia.radio.equalizer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import androidx.preference.PreferenceManager;
import com.cyberia.radio.AppSetup;
import com.cyberia.radio.global.MyAppContext;

public class EqualizerManager
{
    private static EqualizerManager instance;
    private volatile AudioEffects audioEffects;
    private EqualizerFragment equalizerFrag;
    private final static String defaultColor = "#4caf50";


    public static synchronized EqualizerManager getInstance()
    {
        if (instance == null)
            instance = new EqualizerManager();

        return instance;
    }


    public EqualizerManager()
    {
        EqualizerSettings settings = AppSetup.getEQsettings();

        if (settings != null)
            loadEqualizerSettings(settings);
    }

    public synchronized void initEqualizer(int audioSessionID)
    {
        //build new effects object
        audioEffects = new AudioEffects(audioSessionID);
        audioEffects.init();
    }


    public int getCurrentSessionID()
    {
        if (audioEffects != null)
        {
            return audioEffects.audioSessionId;
        } else
            return -1;
    }

    public synchronized EqualizerFragment getEqualizerFragment()
    {
        if(audioEffects != null)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyAppContext.INSTANCE.getAppContext());
            String controlColor = prefs.getString("equalizer_color", defaultColor);

            equalizerFrag = EqualizerFragment.newBuilder()
                    .setAccentColor(Color.parseColor(controlColor))
                    .setAudioSessionId(audioEffects.getAudioSessionId())
                    .build();

            equalizerFrag.setAudioEffects(audioEffects);

            return equalizerFrag;
        } else
            return null;
    }

    public void removeFromSession()
    {
            if (equalizerFrag != null)
            {
                equalizerFrag.closeEQ();
                equalizerFrag = null;
            }

            if (audioEffects != null)
            {
                audioEffects.releaseEffects();
                audioEffects.initAudioSessionId();
                audioEffects = null;
            }
    }

    public EqualizerFragment getEqualizerFrag()
    {
        return equalizerFrag;
    }

    // loaded from Prefs
    private synchronized void loadEqualizerSettings(EqualizerSettings settings)
    {
        EqualizerModel model = new EqualizerModel();
        model.setBassStrength(settings.bassStrength);
        model.setPresetPos(settings.presetPos);
        model.setReverbPreset(settings.reverbPreset);
        model.setSeekbarpos(settings.seekbarpos);
        Settings.isEqualizerReloaded = true;

        Settings.isEqualizerEnabled = settings.isSwitchedOn;
        Settings.bassStrength = settings.bassStrength;
        Settings.presetPos = settings.presetPos;
        Settings.reverbPreset = settings.reverbPreset;
        Settings.seekbarpos = settings.seekbarpos;
        Settings.equalizerModel = model;
    }

    // prepared to be saved in Prefs
    public synchronized EqualizerSettings getCurrentSettings()
    {
        // build settings object and return to be saved in Preferences
        EqualizerSettings settings = null;

        if (Settings.equalizerModel != null)
        {
            settings = new EqualizerSettings();
            settings.bassStrength = Settings.equalizerModel.getBassStrength();
            settings.presetPos = Settings.equalizerModel.getPresetPos();
            settings.reverbPreset = Settings.equalizerModel.getReverbPreset();
            settings.seekbarpos = Settings.equalizerModel.getSeekbarpos();
            settings.isSwitchedOn = Settings.isEqualizerEnabled;
        }

        return settings;
    }

    static class AudioEffects
    {
        public Equalizer mEqualizer;
        public BassBoost bassBoost;
        public LoudnessEnhancer gain;
        public volatile int audioSessionId;

        public AudioEffects(int sessionID)
        {
            audioSessionId = sessionID;
        }

        public int getAudioSessionId()
        {
            return audioSessionId;
        }

        public void initAudioSessionId()
        {
            audioSessionId = 0;
        }

        private void init()
        {
            Settings.isEditing = true;

            if (Settings.equalizerModel == null)
            {
                Settings.equalizerModel = new EqualizerModel();
                Settings.equalizerModel.setReverbPreset((short) (1000 / 19));
                Settings.equalizerModel.setBassStrength((short) (1000 / 19));
            }

            mEqualizer = new Equalizer(0, audioSessionId);
            bassBoost = new BassBoost(0, audioSessionId);
            bassBoost.setEnabled(Settings.isEqualizerEnabled);
            BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
            BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
            bassBoostSetting.strength = Settings.equalizerModel.getBassStrength();
            bassBoost.setProperties(bassBoostSetting);

            gain = new LoudnessEnhancer(audioSessionId);
            gain.setTargetGain(Settings.equalizerModel.getReverbPreset());
            gain.setEnabled(Settings.isEqualizerEnabled);

            mEqualizer.setEnabled(Settings.isEqualizerEnabled);

            if (Settings.presetPos == 0)
            {
                for (short bandIdx = 0; bandIdx < mEqualizer.getNumberOfBands(); bandIdx++)
                {
                    mEqualizer.setBandLevel(bandIdx, (short) Settings.seekbarpos[bandIdx]);
                }
            } else
            {
                mEqualizer.usePreset((short) (Settings.presetPos - 1));
            }
        }

        public void releaseEffects()
        {
            if (mEqualizer != null &&
                    bassBoost != null &&
                    gain != null)
            {
                mEqualizer.release();
                bassBoost.release();
                gain.release();
            }
        }
    }
}


