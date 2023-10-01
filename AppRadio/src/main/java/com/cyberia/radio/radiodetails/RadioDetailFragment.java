package com.cyberia.radio.radiodetails;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.sun.istack.NotNull;

import java.util.ArrayList;
import java.util.Objects;


public class RadioDetailFragment extends Fragment
{
    public interface RadioInfo
    {
        int STATION = 0;
        int GENRE = 1;
        int COUNTRY = 2;
        int LANG = 3;
        int HOMEPAGE = 4;
        int URL_THUMB = 5;
        int BITRATE = 6;
        int CODEC = 7;
    }

    private static final String ARG_TOPIC = "com.cyberia.radio_radio_detail";
    final static String RADIO_INFO = "Info";
    private RadioDetailFragmentView fragmentRadioDetailView;
    private Controller controller;
    private ArrayList<String> details;
    private String prevTitle, prevSubtitle;


    public RadioDetailFragment()
    {
    }

    public static RadioDetailFragment newInstance(ArrayList<String> details)
    {
        RadioDetailFragment fragment = new RadioDetailFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_TOPIC, details);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            details = getArguments().getStringArrayList(ARG_TOPIC);


        ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();

       if (bar != null)
           prevTitle  = Objects.requireNonNull(bar.getTitle()).toString();

        String sub = (String) Objects.requireNonNull(bar).getSubtitle();

        if (TextUtils.isEmpty(sub))
            prevSubtitle = null;
        else
            prevSubtitle = sub;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView;

        if (fragmentRadioDetailView == null)
            fragmentRadioDetailView = new RadioDetailFragmentView(this, inflater, container, getContext());

        rootView = fragmentRadioDetailView.getRootView();

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        showRadioDetails();
    }

    public void showRadioDetails()
    {
       MyThreadPool.INSTANCE.getExecutorService().execute(() -> fragmentRadioDetailView.postRadioDetails(details));
    }

    private void updateAppBar()
    {
        controller.updateAppBarTitle(RADIO_INFO, null, true);
//        MyNavController.setCategory(RADIO_INFO);
    }

    @Override
    public void onPause()
    {
        // MyPrint.printOut(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop()
    {
        // MyPrint.printOut(TAG, "onStop");
        super.onStop();

        MyNavController.updateAppBar(controller, prevTitle, prevSubtitle);
    }

    @Override
    public void onStart()
    {
        // MyPrint.printOut(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume()
    {
        // MyPrint.printOut(TAG, "onResume");
        super.onResume();
        updateAppBar();
    }

    @Override
    public void onDetach()
    {
        MyPrint.printOut("Radio Info", "onDetach");
        super.onDetach();
        controller.slidePanel(MainActivity.PANEL_SLIDE_UP);
        controller = null;
        fragmentRadioDetailView = null;
    }

    @Override
    public void onDestroy()
    {
        // MyPrint.printOut(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        // MyPrint.printOut(TAG, "onAttach");
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
}