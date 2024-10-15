package com.cyberia.radio.stations;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.sfuhrm.radiobrowser4j.Station;


public class StationsFragment extends ListFragment implements MvcViewEventListener
{
    public final String TAG = "StationsFragment";
    private static final String ARG_TOPIC = "selection";
    private static final String ARG_MODE = "search_mode";
    private static final String ARG_COUNTRY_CODE = "country_code";
    private StationsFragmentView stationsFragmentView;
    private Controller controller;
    private volatile StationsFragmentModel stationsModel;
    private volatile ProgressBar progress;
    private final AtomicBoolean isFragmentDirty = new AtomicBoolean(false);
    private volatile int searchMode;
    private String countryCode;
    private String genre;


    public static StationsFragment newInstance(String searchKey, int searchWhat)
    {
        StationsFragment fragment = new StationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC, searchKey);
        args.putInt(ARG_MODE, searchWhat);
        fragment.setArguments(args);
        return fragment;
    }

    public static StationsFragment newInstance(String searchKey, String countryCode, int searchWhat)
    {
        StationsFragment fragment = new StationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC, searchKey);
        args.putString(ARG_COUNTRY_CODE, countryCode);
        args.putInt(ARG_MODE, searchWhat);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            genre = getArguments().getString(ARG_TOPIC);
            searchMode = getArguments().getInt(ARG_MODE);
            countryCode = getArguments().getString(ARG_COUNTRY_CODE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View rootView;

        if (stationsFragmentView == null)
        {
            try
            {
                stationsFragmentView = new StationsFragmentView(this, inflater, container, getContext());
                stationsFragmentView.setListener(this);

                rootView = stationsFragmentView.getRootView();
                progress = rootView.findViewById(R.id.progressStations);

            } catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        rootView = stationsFragmentView.getRootView();

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle hashMap)
    {
        super.onViewCreated(view, hashMap);

        setScrollListener();
        updateAppBar();

        if (stationsModel == null)
        {
            stationsModel = new StationsFragmentModel(StationsFragment.this);
        }

        if (!isFragmentDirty.get())
        {
            loadStations(searchMode);
            isFragmentDirty.set(true);
        }
    }

    private void setScrollListener()
    {
        getListView().setOnScrollListener(new LazyLoader(LazyLoader.DEFAULT_THRESHOLD)
        {
            @Override
            public void loadMore(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (searchMode == GenreFlags.TREND ||
                        searchMode == GenreFlags.RECENT ||
                        searchMode == GenreFlags.SINGLE_STATION) return;

                ListAdapter adapter = requireListAdapter();

                if ((adapter != null && adapter.getCount() > 0))
                    loadStations(searchMode);

//                    if ((adapter != null ? adapter.getCount() : 0) >= StationsFragmentModel.DEFAULT_PAGE_SIZE) //Modified 01/12/22
//                        loadStations(searchMode);
            }
        });
    }

    private void loadStations(int flag)
    {
        if ("chillout".equalsIgnoreCase(genre))
            genre = "chill";

        if ("R&B".equalsIgnoreCase(genre))
            genre = "RnB";

        if ("general".equalsIgnoreCase(genre))
            genre = "talk";


        String searchKey;
        if (flag == GenreFlags.COUNTRY)
            searchKey = countryCode;
        else
        {
            searchKey = genre;
        }

        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
            int listStart = stationsModel.getCount();

//            if(listStart == 0) listStart++;

            stationsModel.readLazy(searchKey, listStart, flag, StationsFragmentModel.DEFAULT_PAGE_SIZE);
        });
    }


    public void onStationsAvailable(List<Station> stationsList, int count)
    {
        cancelProgress();


        if (!CollectionUtils.isEmpty(stationsList))
        {
            List<StationCookie> list = new ArrayList<>();

            for (Station station : stationsList)  //TODO: change StationFragment
                list.add(new StationCookie(station));

            stationsFragmentView.showStations(list);
        } else
        {
            if (count < 1)
                MyHandler.post(() -> Toast.makeText(requireActivity(),
                        "Stations not found.", Toast.LENGTH_LONG).show());
        }
    }


    @Override
    public void onListItemClick(ListView listView, @NonNull View view, final int position, long id)
    {
        StationCookie cookie = (StationCookie) listView.getItemAtPosition(position);
        controller.onStationInfoAvailable(cookie);
    }

    private void updateAppBar()
    {
//        MyPrint.printOut(TAG, "Update bar with " + genre);

        if (searchMode == GenreFlags.SINGLE_STATION)
            controller.updateAppBarTitle(GenreFlags.CAT_SHARE, null, true);
        else if (searchMode == GenreFlags.TREND)
            controller.updateAppBarTitle(GenreFlags.CAT_TREND, null, true);
        else
        {
            controller.updateAppBarTitle(MyNavController.getCategory(), genre, true);
        }
    }

    void cancelProgress()
    {
        MyHandler.post(() -> {
            if (progress != null)
            {
                progress.setVisibility(ProgressBar.GONE);
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onDestroyView()
    {
        MyPrint.printOut(getClass().getSimpleName(), "onDestroyView is called");
        super.onDestroyView();
        stationsFragmentView = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stationsModel = null;
        controller = null;
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
    public void onViewEvent(String s)
    {
    }
}


