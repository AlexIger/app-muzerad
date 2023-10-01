package com.cyberia.radio.genres;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;

import java.util.List;


public class GenreFragment extends ListFragment implements MvcViewEventListener
 {
     private static final String ARG_CATEGORY = "category_selection";
     private int selection;
     private GenreFragmentView genreFragmentView;
     private Controller controller;
     private GenresModel genreModel;
     private Parcelable state;


    public GenreFragment()
    {
    }

    public static GenreFragment newInstance(int genreSelection)
    {
        GenreFragment fragment = new GenreFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY, genreSelection);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            selection = getArguments().getInt(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView;

        if(genreFragmentView == null)
        {
            genreFragmentView = new GenreFragmentView(this, inflater, container);
            genreFragmentView.setListener(this);
        }

        rootView = genreFragmentView.getRootView();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if(genreModel == null)
        {
            genreModel = new GenresModel(this);
            genreModel.readGenres(selection);
        }
    }

     private void updateAppBar(final String title)
     {
         controller.updateAppBarTitle(title, null, true);
         MyNavController.setCategory(title);
     }

     @Override
     public void onStart()
     {
         super.onStart();

         updateAppBar(GenreFlags.RadioCategories[selection]);
     }

    public void onGenresAvailable(List <String> genreList, SimpleArrayMap<Integer, Drawable> categories)
    {
//        cancelProgress();
        genreFragmentView.showGenres(getContext(), genreList, categories);
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id)
    {
        String selection = String.valueOf(getListView().getItemAtPosition(position));
        showStationsScreen(selection);
    }

     void showStationsScreen(String stationsForGenre)
     {
         //controller = MainActivity
         controller.addStationScreenFragment(stationsForGenre, GenreFlags.STATIONS);
     }

     @Override
     public void onPause()
     {
         super.onPause();
         state =  getListView().onSaveInstanceState();
     }


     @Override
     public void onResume()
     {
         super.onResume();

         if (state != null)
         {
             getListView().onRestoreInstanceState(state);
         }
     }


    @Override
    public void onDetach()
    {
        super.onDetach();
        controller = null;
        genreFragmentView = null;
        genreModel = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onViewEvent(String s)
    {
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
}


