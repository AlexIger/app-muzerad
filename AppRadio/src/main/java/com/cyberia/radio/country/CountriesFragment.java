package com.cyberia.radio.country;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.google.android.gms.common.util.CollectionUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;


public class CountriesFragment extends Fragment implements MvcViewEventListener
{
    private CountriesFragmentView fragView;
    private Controller controller;
    private volatile CountriesModel countriesModel;
    private ProgressBar progress;
    private volatile Parcelable state;

    public static CountriesFragment newInstance()
    {
        return new CountriesFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView;

        if (fragView == null)
        {
            try
            {
                fragView = new CountriesFragmentView(this, container);
                fragView.setListener(this);

                rootView = fragView.getRootView();
                progress = rootView.findViewById(R.id.progress_countries);
            } catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        rootView = fragView.getRootView();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (countriesModel == null)
        {
            countriesModel = new CountriesModel(this);
            countriesModel.getCountryNames();
        }

        updateAppBar();
    }

    public void onCountriesAvailable(List<CountriesModel.Country> countriesList)
    {
        cancelProgress();

        if (CollectionUtils.isEmpty(countriesList))
        {
            MyHandler.post(() -> Toast.makeText(getActivity(), "Countries not found", Toast.LENGTH_LONG).show());
        } else
        {
            fragView.showCountries(countriesList, clickListener);
        }
    }


    private void updateAppBar()
    {
        controller.updateAppBarTitle(GenreFlags.CAT_BYCOUNTRY, null, true); //TODO
//        controller.setBackButton(true);
        MyNavController.setCategory(GenreFlags.CAT_BYCOUNTRY);
    }

    void cancelProgress()
    {
        requireActivity().runOnUiThread(() -> {
            if (progress != null)
            {
                progress.setVisibility(ProgressBar.GONE);
            }
        });
    }

    @Override
    public void onStop()
    {
        MyPrint.printOut(getClass().getSimpleName(), "onStop");
        super.onStop();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        state = Objects.requireNonNull(fragView.getRecyclerView().getLayoutManager()).onSaveInstanceState();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (state != null)
        {
            Objects.requireNonNull(fragView.getRecyclerView().getLayoutManager(), "Must be NonNull").onRestoreInstanceState(state);
        }
    }


    @Override
    public void onDetach()
    {
        MyPrint.printOut(getClass().getSimpleName(), " onDetach");
        super.onDetach();
        controller = null;
        fragView = null;
        countriesModel = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
    public void onViewEvent(String s)
    {
    }

    CountriesFragmentView.RecyclerViewClickListener clickListener = new CountriesFragmentView.RecyclerViewClickListener()
    {
        @Override
        public void onRecyclerItemClick(CountriesModel.Country country)
        {
            controller.addStationScreenFragment(country.name, country.code, GenreFlags.COUNTRY);
        }
    };
}


