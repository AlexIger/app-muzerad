package com.cyberia.radio.settings;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.R;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.interfaces.Controller;

import java.util.Objects;

public class PrefsFragment extends PreferenceFragmentCompat
{
    private Controller controller;
    static final String SETTINGS_SCREEN = "Settings";
    private String prevTitle, prevSubtitle;

    public static PrefsFragment newInstance()
    {

        return new PrefsFragment();
    }

    public PrefsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
        prevTitle = (String) Objects.requireNonNull(bar).getTitle();

        String subtitle = (String) bar.getSubtitle();
        if (TextUtils.isEmpty(subtitle))
            prevSubtitle = null;
        else
            prevSubtitle = subtitle;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        updateAppBar();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground));
        return view;
    }

//    private int getDefaultColor()
//    {
//        TypedValue typedValue = new TypedValue();
//
//        if (getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true))
//            return typedValue.data;
//        else
//            return  ContextCompat.getColor(getContext(), R.color.windowBackground);
//    }

    private void updateAppBar()
    {
        controller.updateAppBarTitle(SETTINGS_SCREEN, null, true);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof Controller)
        {
            controller = (Controller) context;
        } else
        {
            throw new RuntimeException(context
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        MyNavController.updateAppBar(controller, prevTitle, prevSubtitle);
    }
}
