package com.cyberia.radio.history;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.persistent.Station;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.List;
import java.util.Objects;


public class HistoryFragment extends ListFragment implements MvcViewEventListener
{
    public final String HISTORY_CLEARED = "Cleared all";
    private HistoryFragmentView historyFragmentView;
    private Controller controller;
    private Parcelable state;



    public static HistoryFragment newInstance()
    {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View rootView = null;

        if (historyFragmentView == null)
        {
            try
            {
                historyFragmentView = new HistoryFragmentView(this, inflater, container, getContext());
                historyFragmentView.setListener(this);

                rootView = historyFragmentView.getRootView();
            }
            catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle hashMap)
    {
        controller.updateAppBarTitle(GenreFlags.CAT_RECENT, null, true);
        registerForContextMenu(getListView());
        observeData();

        super.onViewCreated(view, hashMap);
    }



    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        requireActivity().getMenuInflater().inflate(R.menu.menu_context_history, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Station station = (Station) Objects.requireNonNull(getListAdapter()).getItem(info.position);

        if (checkIfEmpty(station))
            return true;

        if (item.getItemId() == R.id.menu_station_play)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
                    controller.onStationInfoAvailable(new StationCookie(station)));

        }
        else if (item.getItemId() == R.id.menu_remove)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
            {
                HistoryModel model = new ViewModelProvider(this).get(HistoryModel.class);
                model.deleteHistoryStation(station);
            });
        }
        else if (item.getItemId() == R.id.menu_clear_history)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() ->
            {
                HistoryModel model = new ViewModelProvider(this).get(HistoryModel.class);
                model.deleteAllHistoryStations();

            });
            Toast.makeText(getActivity(), HISTORY_CLEARED, Toast.LENGTH_LONG).show();
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
        Station station = (Station) getListView().getItemAtPosition(position);
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
        MyPrint.printOut(getClass().getSimpleName(), "onAttach is called");

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
        MyPrint.printOut(getClass().getSimpleName(), "onDetach is called");
        super.onDetach();
    }

    @Override
    public void onDestroyView()
    {
        MyPrint.printOut(getClass().getSimpleName(), "onDestroyView is called");
        super.onDestroyView();
        historyFragmentView = null;
    }

    @Override
    public void onDestroy()
    {
        MyPrint.printOut(getClass().getSimpleName(), "onDestroy is called");
        super.onDestroy();
        controller = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        state = getListView().onSaveInstanceState();
    }

    @Override
    public void onResume()
    {
        MyPrint.printOut(getClass().getSimpleName(), "onResume is called");

        super.onResume();

        if (state != null)
        {
            getListView().onRestoreInstanceState(state);
        }
    }

    @Override
    public void onViewEvent(String s)
    {
    }

    public void observeData()
    {
        HistoryModel model = new ViewModelProvider(this).get(HistoryModel.class);
        model.getAllHistoryStations().observe(getViewLifecycleOwner(), new Observer<List<Station>>()
        {
            @Override
            public void onChanged(List<Station> station)
            {
                if (CollectionUtils.isEmpty(station)) station.add(new Station(getString(R.string.empty_list)));

                historyFragmentView.showHistory(station);

                MyPrint.printOut(getClass().getSimpleName(), "onShowHistory is called");
            }
        });
    }
}
