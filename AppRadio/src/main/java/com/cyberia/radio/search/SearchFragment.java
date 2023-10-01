package com.cyberia.radio.search;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.R;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.sfuhrm.radiobrowser4j.Station;

public class SearchFragment extends ListFragment implements
        MvcViewEventListener,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener
{
    private final static String TAG = "SearchFragment";
    private final static String SEARCH_SCREEN = "Search";
    private final static String STATION_NOT_FOUND = "Stations not found.";
    private SearchFragmentView searchFragmentView;
    private Controller controller;
    private SearchFragmentModel searchModel;
    private volatile ProgressBar progress;
    private final AtomicBoolean isFragmentDirty = new AtomicBoolean(false);
    private final AtomicBoolean loadAgain = new AtomicBoolean(true);
    private SearchView searchView;
    private final Object lock = new Object();


    public static SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchViewItem.getActionView();
        searchView.setQuery("", false);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
    }

    @Override
    public boolean onClose()
    {
        loadAgain.compareAndSet(false, true);

        return false;
    }

    public void initialFilter()
    {
       MyThreadPool.INSTANCE.getExecutorService().execute(new Runnable()
        {
            final SearchFragmentView.StationsListAdapter adapter =
                    (SearchFragmentView.StationsListAdapter) getListAdapter();

            public void run()
            {
                MyHandler.getHandler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (searchView != null)
                        {
                            CharSequence query = searchView.getQuery();
                            if (adapter != null && !TextUtils.isEmpty(query))
                                adapter.getFilter().filter(query);
                        }
                    }
                }, 500);
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query)
    {
        SearchFragmentView.StationsListAdapter adapter =
                (SearchFragmentView.StationsListAdapter) getListAdapter();

        // starts search when the characters are 3, no white space, and load again flag is true
        if (query.length() == 3 && !query.matches(".*\\s.*") && loadAgain.get())
        {
            startProgress();
            loadStations(query);
            loadAgain.set(false);
        }

        synchronized (lock)
        {
            if (adapter != null)
            {
                if (query.length() > 0)
                {
                    adapter.getFilter().filter(query);
                } else
                {
                    loadAgain.set(true);
                    adapter.clearData();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        }

        return false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View rootView = null;

        if (searchFragmentView == null)
        {
            try
            {
                searchFragmentView = new SearchFragmentView(this, inflater, container, getContext());
                searchFragmentView.setListener(this);

                rootView = searchFragmentView.getRootView();
                progress = rootView.findViewById(R.id.progressStations);

            } catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle hashMap)
    {
        super.onViewCreated(view, hashMap);

        MyPrint.printOut(TAG, " onViewCreated");

        cancelProgress();
        updateAppBar();

        if (searchModel == null)
        {
            searchModel = new SearchFragmentModel(this);
        }

        isFragmentDirty.set(true);


    }

    void loadStations(String searchKey)
    {
        searchModel.readStations(searchKey);
    }


    public void onStationsAvailable(List<Station> stationsList)
    {
        cancelProgress();
        List<StationCookie> list = new ArrayList<>();

        if (CollectionUtils.isEmpty(stationsList))
        {
            MyHandler.getHandler().post(() ->
                    Toast.makeText(MyAppContext.INSTANCE.getAppContext(), STATION_NOT_FOUND, Toast.LENGTH_LONG).show());
        } else
        {
            for (Station station: stationsList)
            {
                list.add(new StationCookie(station));
            }

            searchFragmentView.showStations(list);
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, final int position, long id)
    {
        StationCookie cookie = (StationCookie) getListView().getItemAtPosition(position);
        controller.onStationInfoAvailable(cookie);
    }

    private void updateAppBar()
    {
        controller.updateAppBarTitle(SEARCH_SCREEN, null, true);
    }

    void cancelProgress()
    {
        MyHandler.getHandler().post(() -> {
            if (progress != null)
            {
                progress.setVisibility(ProgressBar.GONE);
            }
        });
    }

    void startProgress()
    {
        MyHandler.getHandler().postDelayed(() -> {
            if (progress != null)
            {
                progress.setVisibility(ProgressBar.VISIBLE);
            }
        }, 500);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        MyPrint.printOut(TAG, " onStop");
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadAgain.set(true);

        MyPrint.printOut(TAG, " onStop");

    }


    @Override
    public void onDetach()
    {
        super.onDetach();
        controller = null;
        searchModel = null;
        MyPrint.printOut(TAG, " onDetach");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        searchModel = null;
        searchFragmentView = null;

        MyPrint.printOut(TAG, " onDestroy");
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
}