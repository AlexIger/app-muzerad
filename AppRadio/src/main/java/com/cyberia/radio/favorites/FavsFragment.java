package com.cyberia.radio.favorites;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.persistent.Station;
import com.cyberia.radio.persistent.Repository;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
//import com.cyberia.radio.playlist.PlaylistManager;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.List;
import java.util.Objects;


public class FavsFragment extends ListFragment implements MvcViewEventListener
{
    private FavsFragmentView favsFragmentView;
    private Controller controller;
    private volatile ProgressBar progress;
    private List<Station> list;


    public static FavsFragment newInstance()
    {

        return new FavsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View rootView;

        if (favsFragmentView == null)
        {
            try
            {
                favsFragmentView = new FavsFragmentView(this, inflater, container, getContext());
                favsFragmentView.setListener(this);

                rootView = favsFragmentView.getRootView();
                progress = rootView.findViewById(R.id.progressStations);



            }
            catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        rootView = favsFragmentView.getRootView();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle hashMap)
    {
        controller.updateAppBarTitle(GenreFlags.CAT_FAVS, null, true);
//        getAllFromFavorites();
        registerForContextMenu(getListView());
        observeData();

        super.onViewCreated(view, hashMap);
    }

    private void getAllFromFavorites()
    {
        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
//            list = FavsFragmentModel.getFavorites();
            cancelProgress();

            if (CollectionUtils.isEmpty(list))
            {
                //              MyPrint.printOut("FavoritesFragment", "List is null");
                list.add(new Station(getString(R.string.empty_list)));
            }
            //                    MyPrint.printOut("FavoritesFragment", "Showing Favorites");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            boolean isFavoritesSorted = prefs.getBoolean("sort_favorites", false);

            if (isFavoritesSorted)
            {
                list.sort((station1, station2) -> station1.getTitle().compareToIgnoreCase(station2.getTitle()));
            }

//            favsFragmentView.showView(list);
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        requireActivity().getMenuInflater().inflate(R.menu.menu_context_favorites, menu);
        //        menu.setHeaderTitle("Options");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Station station = (Station) Objects.requireNonNull(getListAdapter()).getItem(Objects.requireNonNull(info).position);

        if (checkIfEmpty(station))
            return false;

        StationCookie cookie = new StationCookie(station);

        if (item.getItemId() == R.id.menu_station_play)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() -> controller.onStationInfoAvailable(cookie));
        }
        else if (item.getItemId() == R.id.menu_remove)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
            {
                FavsFragmentModel model = new ViewModelProvider(this).get(FavsFragmentModel.class);
                model.deleteFavorite(station);
            });

        }
        else if (item.getItemId() == R.id.menu_clear_history)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
            {
                FavsFragmentModel model = new ViewModelProvider(this).get(FavsFragmentModel.class);
                model.deleteAllFavorites();
            });

            Toast.makeText(getActivity(), "Cleared all", Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.menu_add_to_playlist)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                    Repository.getInstance().insertPlaylistStation(cookie));

            Toast.makeText(getActivity(), "Added to playlist", Toast.LENGTH_SHORT).show();
        }
        else
        {
            return false;
        }

        return true;
    }


    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id)
    {
        final Station station = (Station) getListView().getItemAtPosition(position);
        if (checkIfEmpty(station))
            return;

        MyThreadPool.INSTANCE.getExecutorService().execute(() ->
        {
            if (station != null)
            {
                StationCookie cookie = new StationCookie(station);
                controller.onStationInfoAvailable(cookie);
            }
        });
    }

    void cancelProgress()
    {
        requireActivity().runOnUiThread(() ->
        {
            if (progress != null)
            {
                progress.setVisibility(ProgressBar.GONE);
            }
        });
    }

    private boolean checkIfEmpty(Station entry)
    {
        if (entry == null || "empty".equalsIgnoreCase(entry.getTitle()))
        {
            Toast.makeText(MyAppContext.INSTANCE.getAppContext(), "Empty selection", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof Controller)
        {
            controller = (Controller) context;
        }
        else
        {
            throw new RuntimeException(context
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        controller = null;

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
        favsFragmentView = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onViewEvent(String s)
    {
    }

    public void observeData()
    {
        FavsFragmentModel model = new ViewModelProvider(this).get(FavsFragmentModel.class);

        model.getAll().observe(getViewLifecycleOwner(), new Observer<List<Station>>()
        {
            @Override
            public void onChanged(List<Station> stations)
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                boolean isFavoritesSorted = prefs.getBoolean("sort_favorites", false);

                if (CollectionUtils.isEmpty(stations)) stations.add(new Station(getString(R.string.empty_list)));

                if (isFavoritesSorted)
                    stations.sort((station1, station2) -> station1.getTitle().compareToIgnoreCase(station2.getTitle()));

                favsFragmentView.showView(stations);
            }
        });
    }
}
