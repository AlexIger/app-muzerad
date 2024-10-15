package com.cyberia.radio.equalizer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.R;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment
{
    public static final String ARG_AUDIO_SESSION_ID = "audio_session_id";

    private LineSet dataset;
    private LineChartView chart;
    private final SeekBar[] seekBarFinal = new SeekBar[5];
    private final TextView[] textViewFinal = new TextView[5];

    private float[] points;

    private AnalogController bassController, gainController; //GAIN
    private volatile Spinner presetSpinner;
    private Context ctx;

    private Equalizer mEqualizer;
    private BassBoost bassBoost;
    private LoudnessEnhancer presetReverb;

    static int themeColor = Color.parseColor("#B24242");
    static boolean showBackButton = true;

    public EqualizerFragment()
    {
        // Required empty public constructor
    }

    public static EqualizerFragment newInstance(int audioSessionId)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_AUDIO_SESSION_ID, audioSessionId);

        EqualizerFragment fragment = new EqualizerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setAudioEffects(EqualizerManager.AudioEffects effects)
    {
        mEqualizer = effects.mEqualizer;
        bassBoost = effects.bassBoost;
        presetReverb = effects.gain;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Settings.isEditing = true;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        ImageView backBtn = view.findViewById(R.id.equalizer_back_btn);
        backBtn.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getActivity() != null)
                {
                    getActivity().onBackPressed();
                }
            }
        });

        SwitchCompat equalizerSwitch = view.findViewById(R.id.equalizer_switch);
        equalizerSwitch.setChecked(Settings.isEqualizerEnabled);
        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                mEqualizer.setEnabled(isChecked);
                bassBoost.setEnabled(isChecked);
                presetReverb.setEnabled(isChecked);
                Settings.isEqualizerEnabled = isChecked;
                Settings.equalizerModel.setEqualizerEnabled(isChecked);

                onEqStateControlColors();
                onEqStateSeekBarKnobs();
                onSpinnerStateChange();
            }
        });

        presetSpinner = view.findViewById(R.id.equalizer_preset_spinner);
        ImageView spinnerDropDownIcon = view.findViewById(R.id.spinner_dropdown_icon);
        spinnerDropDownIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (Settings.isEqualizerEnabled)
                    presetSpinner.performClick();
            }
        });


        chart = view.findViewById(R.id.lineChart);
        Paint paint = new Paint();
        dataset = new LineSet();

        bassController = view.findViewById(R.id.controllerBass);
        gainController = view.findViewById(R.id.controller3D);

        bassController.setLabel("BASS");
        gainController.setLabel("GAIN");

        int x;
        int y;
        if (!Settings.isEqualizerReloaded)
        {
            x = 0;
            y = 0;
            if (bassBoost != null)
            {
                try
                {
                    x = ((bassBoost.getRoundedStrength() * 19) / 1000);
                } catch (Exception e)
                {
//                    e.printStackTrace();
                }
            }

            if (presetReverb != null)
            {
                try
                {
                    y = (int) (presetReverb.getTargetGain() * 19) / 1000;
                } catch (Exception e)
                {
//                    e.printStackTrace();
                }
            }

        } else
        {
            x = ((Settings.bassStrength * 19) / 1000);
            y = ((Settings.reverbPreset * 19) / 1000);

        }
        if (x == 0)
        {
            bassController.setProgress(1);
        } else
        {
            bassController.setProgress(x);
        }
        if (y == 0)
        {
            gainController.setProgress(1);
        } else
        {
            gainController.setProgress(y);
        }

        bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener()
        {
            @Override
            public void onProgressChanged(int progress)
            {
                Settings.bassStrength = (short) (((float) 1000 / 19) * (progress));
                try
                {
                    bassBoost.setStrength(Settings.bassStrength);
                    Settings.equalizerModel.setBassStrength(Settings.bassStrength);
                } catch (Exception e)
                {
//                    e.printStackTrace();
                }
            }
        });

        gainController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener()
        {
            @Override
            public void onProgressChanged(int progress)
            {
                Settings.reverbPreset = (short) (((float) 1000 / 19) * (progress));
                try
                {
                    presetReverb.setTargetGain(Settings.reverbPreset);
                    Settings.equalizerModel.setReverbPreset(Settings.reverbPreset);
                } catch (Exception e)
                {
//                    e.printStackTrace();
                }
//                y = progress;
            }
        });

//        LinearLayout mLinearLayout = view.findViewById(R.id.equalizerContainer);

        TextView equalizerHeading = new TextView(getContext());
        equalizerHeading.setText(R.string.eq);
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

        short numberOfFrequencyBands = 5;

        points = new float[numberOfFrequencyBands];

        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < numberOfFrequencyBands; i++)
        {
            final short equalizerBandIndex = i;
            final TextView frequencyHeaderTextView = new TextView(getContext());
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"));
            frequencyHeaderTextView.setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");

            LinearLayout seekBarRowLayout = new LinearLayout(getContext());
            seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

            TextView lowerEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

            TextView upperEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

            SeekBar seekBar = new SeekBar(getContext());
            TextView textView = new TextView(getContext());
            switch (i)
            {
                case 0:
                    seekBar = view.findViewById(R.id.seekBar1);
                    textView = view.findViewById(R.id.textView1);
                    break;
                case 1:
                    seekBar = view.findViewById(R.id.seekBar2);
                    textView = view.findViewById(R.id.textView2);
                    break;
                case 2:
                    seekBar = view.findViewById(R.id.seekBar3);
                    textView = view.findViewById(R.id.textView3);
                    break;
                case 3:
                    seekBar = view.findViewById(R.id.seekBar4);
                    textView = view.findViewById(R.id.textView4);
                    break;
                case 4:
                    seekBar = view.findViewById(R.id.seekBar5);
                    textView = view.findViewById(R.id.textView5);
                    break;
            }
            seekBarFinal[i] = seekBar;
            textViewFinal[i] = textView;


            seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN));
            seekBar.getThumb().setColorFilter(new PorterDuffColorFilter
                    (Settings.isEqualizerEnabled ? themeColor : Color.DKGRAY, PorterDuff.Mode.SRC_IN));
            seekBar.setId(i);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
            seekBar.setEnabled(Settings.isEqualizerEnabled);

            textView.setText(frequencyHeaderTextView.getText());
            textView.setTextColor((Settings.isEqualizerEnabled ? Color.WHITE : Color.DKGRAY));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


            if (Settings.isEqualizerReloaded)
            {
                points[i] = Settings.seekbarpos[i] - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(Settings.seekbarpos[i] - lowerEqualizerBandLevel);
            } else
            {
                points[i] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                Settings.seekbarpos[i] = mEqualizer.getBandLevel(equalizerBandIndex);
                Settings.isEqualizerReloaded = true;
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                    points[seekBar.getId()] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                    Settings.seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    Settings.equalizerModel.getSeekbarpos()[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    dataset.updateValues(points);
                    chart.notifyDataUpdate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    presetSpinner.setSelection(0);
                    Settings.presetPos = 0;
                    Settings.equalizerModel.setPresetPos(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });
        }

        equalizeSound();

        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.EqPaintColor, null));
        paint.setStrokeWidth((float) (1.10 * Settings.ratio));

//        dataset.setColor(themeColor);
        dataset.setSmooth(true);
        dataset.setThickness(5);

        chart.setXAxis(false);
        chart.setYAxis(false);

        chart.setYLabels(AxisController.LabelPosition.NONE);
        chart.setXLabels(AxisController.LabelPosition.NONE);
        chart.setGrid(ChartView.GridType.NONE, 7, 10, paint);

        chart.setAxisBorderValues(-300, 3300);

        chart.addData(dataset);
        chart.show();

        onEqStateControlColors();
        onSpinnerStateChange();
    }

    public void equalizeSound()
    {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<>(ctx,
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);

        equalizerPresetNames.add("Custom");

        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++)
        {
            equalizerPresetNames.add(mEqualizer.getPresetName(i));
        }
        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        //presetSpinner.setDropDownWidth((Settings.screen_width * 3) / 4);
        if (Settings.isEqualizerReloaded && Settings.presetPos != 0)
        {
//            correctPosition = false;
            presetSpinner.setSelection(Settings.presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                try
                {
                    if (position != 0)
                    {
                        mEqualizer.usePreset((short) (position - 1));
                        Settings.presetPos = position;
                        short numberOfFreqBands = 5;

                        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

                        for (short i = 0; i < numberOfFreqBands; i++)
                        {
                            seekBarFinal[i].setProgress(mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            points[i] = mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel;
                            Settings.seekbarpos[i] = mEqualizer.getBandLevel(i);
                            Settings.equalizerModel.getSeekbarpos()[i] = mEqualizer.getBandLevel(i);
                        }
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }
                } catch (Exception e)
                {
                    Toast.makeText(ctx, "Error updating Equalizer", Toast.LENGTH_SHORT).show();
                    ExceptionHandler.onException(getClass().getSimpleName(), e);
                }
                Settings.equalizerModel.setPresetPos(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }

        });

        presetSpinner.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (Settings.isEqualizerEnabled)
                {
                    presetSpinner.performClick();
                    return false;
                }
                return true;
            }
        });
    }

    private void onEqStateSeekBarKnobs()
    {
        for (short i = 0; i < seekBarFinal.length; i++)
        {
            seekBarFinal[i].setEnabled(Settings.isEqualizerEnabled);
            seekBarFinal[i].getThumb().setColorFilter(new PorterDuffColorFilter
                    (Settings.isEqualizerEnabled ? themeColor : Color.DKGRAY, PorterDuff.Mode.SRC_IN));
            textViewFinal[i].setTextColor
                    ((Settings.isEqualizerEnabled ? Color.WHITE : Color.DKGRAY));
            seekBarFinal[i].invalidate();
            textViewFinal[i].invalidate();
        }
    }

    private void onSpinnerStateChange()
    {
        try
        {
            ImageView spinnerDropDownIcon = requireView().findViewById(R.id.spinner_dropdown_icon);
            TextView title = requireView().findViewById(R.id.equalizer_fragment_title);
            int position = presetSpinner.getSelectedItemPosition();
            presetSpinner.setSelection(position, true);
            TextView tv = (TextView) presetSpinner.getSelectedView();

            if (!Settings.isEqualizerEnabled)
            {
                spinnerDropDownIcon.setImageAlpha(MainActivity.BUTTON_OPACITY);
                tv.setTextColor(Color.DKGRAY);
                title.setTextColor(Color.DKGRAY);
            } else
            {
                spinnerDropDownIcon.setImageAlpha(0xFF);
                tv.setTextColor(Color.WHITE);
                title.setTextColor(Color.WHITE);
            }
            presetSpinner.invalidate();
        } catch (IllegalStateException e)
        {
            ExceptionHandler.onException("EqFrag", e);
        }
    }

    private void onEqStateControlColors()
    {
        if (!Settings.isEqualizerEnabled)
        {
            bassController.circlePaint2.setColor(Color.DKGRAY);
            gainController.circlePaint2.setColor(Color.DKGRAY);

            bassController.linePaint.setColor(Color.DKGRAY);
            gainController.linePaint.setColor(Color.DKGRAY);

            bassController.textPaint.setColor(Color.DKGRAY);
            gainController.textPaint.setColor(Color.DKGRAY);
            dataset.setColor(Color.DKGRAY);
        } else
        {
            bassController.circlePaint2.setColor(themeColor);
            gainController.circlePaint2.setColor(themeColor);

            bassController.linePaint.setColor(themeColor);
            gainController.linePaint.setColor(themeColor);

            bassController.textPaint.setColor(Color.WHITE);
            gainController.textPaint.setColor(Color.WHITE);
            dataset.setColor(themeColor);
        }

        bassController.setEnabled(Settings.isEqualizerEnabled);
        gainController.setEnabled(Settings.isEqualizerEnabled);

        bassController.invalidate();
        gainController.invalidate();
        presetSpinner.invalidate();
        chart.invalidate();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Settings.isEditing = false;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private int id = -1;

        public Builder setAudioSessionId(int id)
        {
            this.id = id;
            return this;
        }

        public Builder setAccentColor(int color)
        {
            themeColor = color;
            return this;
        }

        public EqualizerFragment build()
        {
            return EqualizerFragment.newInstance(id);
        }
    }

    public void closeEQ()
    {
        MyHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentActivity fragmentActivity = getActivity();

                if (fragmentActivity != null)
                {
                    fragmentActivity.onBackPressed();
                }
            }
        });
    }
}





