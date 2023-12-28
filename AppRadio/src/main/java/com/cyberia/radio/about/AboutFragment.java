package com.cyberia.radio.about;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.R;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutFragment extends Fragment
{
    private static final String ABOUT_SCREEN = "About";
    private static final String VERSION = "7.2";

    public static AboutFragment newInstance()
    {
        return new AboutFragment();
    }

    private String prevTitle, prevSubtitle;
    private Controller controller;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String subtitle = null;
        ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();

        if (bar !=null)
        {
            prevTitle = (String) bar.getTitle();
            subtitle = (String) bar.getSubtitle();
        }

        if (TextUtils.isEmpty(subtitle))
            prevSubtitle = null;
        else
            prevSubtitle = subtitle;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        Element versionElement = new Element();
        versionElement.setTitle(VERSION);

        Element elementLicense = new Element();
        elementLicense.setTitle(getString(R.string.about_app_license));

        Element element1 = new Element();
        element1.setTitle(getString(R.string.about_credits_item_1));

        Element element2 = new Element();
        element2.setTitle(getString(R.string.about_credits_item_2));

        Element element3 = new Element();
        element3.setTitle(getString(R.string.about_credits_item_3));

        Element element4 = new Element();
        element4.setTitle(getString(R.string.about_credits_item_4));

        Element element5 = new Element();
        element5.setTitle(getString(R.string.about_credits_item_5));

        Element element6 = new Element();
        element6.setTitle(getString(R.string.about_credits_item_6));

        return new AboutPage(getContext(), R.style.about_About)
                .isRTL(false)
                .setCustomFont(ResourcesCompat.getFont(requireContext(), R.font.lato))
                .setImage(R.mipmap.ic_app_notify_background)
                .setDescription(getString(R.string.about_description))
                .addGroup("Version")
                .addItem(versionElement)
                .addGroup("License")
                .addItem(elementLicense)
                .addGroup("Credits")
                .addItem(element1)
                .addItem(element2)
                .addItem(element3)
                .addItem(element4)
                .addItem(element5)
                .addItem(element6)
                .addGroup("Contacts")
                .addEmail("muzeradio.info@gmail.com", "Muze team")
//                .addPlayStore("com.ideashower.readitlater.pro")
                .create();
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
    public void onStop()
    {
        MyPrint.printOut("About", " #onStop");
        super.onStop();

        MyNavController.updateAppBar(controller, prevTitle, prevSubtitle);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MyPrint.printOut("About", " #onResume");

        controller.updateAppBarTitle(ABOUT_SCREEN, null, true);
    }

    @Override
    public void onDetach()
    {
        // MyPrint.printOut(TAG, "onDetach");
        super.onDetach();
//        controller.slidePanel(MainActivity.PANEL_SLIDE_UP);
        controller = null;
    }
}