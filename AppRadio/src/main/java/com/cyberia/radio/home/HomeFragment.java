package com.cyberia.radio.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;

public class HomeFragment extends ListFragment implements MvcViewEventListener
{
    private HomeFragmentView homeFragmentView;
    private Controller controller;

    public HomeFragment(){}

    public static HomeFragment newInstance()
    {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        homeFragmentView = new HomeFragmentView(this, inflater, container);
        homeFragmentView.setListener(this);

        return homeFragmentView.getRootView();
    }

    private void updateAppBar(final String title)
    {
        controller.updateAppBarTitle(title, null, false);
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id)
    {
        switch (position)
        {
            case GenreFlags.FAVS -> controller.addFavsScreenFragment();
            case GenreFlags.TREND -> controller.addStationScreenFragment(GenreFlags.CAT_TREND, GenreFlags.TREND);
            case GenreFlags.COUNTRY -> controller.addCountriesScreenFragment();
            case GenreFlags.LANGUAGE -> controller.addLangsFragment();
            case GenreFlags.RECENT -> controller.addHistoryFragment();
            case GenreFlags.SEARCH -> controller.addSearchFragment();
            default -> controller.addGenreScreenFragment(position);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateAppBar(getString(R.string.appbar_home));
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
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        controller = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        homeFragmentView = null;
    }

    @Override
    public void onViewEvent(String s)
    {
    }
}

