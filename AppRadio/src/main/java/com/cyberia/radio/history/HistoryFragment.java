package com.cyberia.radio.history;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
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

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;

import java.util.List;
import java.util.Objects;


public class HistoryFragment extends ListFragment implements MvcViewEventListener
{
    public final String HISTORY_CLEARED = "Cleared all";
    private HistoryFragmentView historyFragmentView;
    private Controller controller;
    private volatile ProgressBar progress;
    private Parcelable state;
    private volatile List<StationCookie> list;


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

        View rootView;

        if (historyFragmentView == null)
        {
            try
            {
                historyFragmentView = new HistoryFragmentView(this, inflater, container, getContext());
                historyFragmentView.setListener(this);

                rootView = historyFragmentView.getRootView();
                progress = rootView.findViewById(R.id.progressStations);
            } catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }

        rootView = historyFragmentView.getRootView();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle hashMap)
    {
        controller.updateAppBarTitle(GenreFlags.CAT_RECENT, null, true);
        displayHistory();
        registerForContextMenu(getListView());

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
        StationCookie cookie = (StationCookie) Objects.requireNonNull(getListAdapter()).getItem(info.position);

        if (checkIfEmpty(cookie))
            return true;

        if (item.getItemId() == R.id.menu_station_play)
        {
            MyThreadPool.INSTANCE.getExecutorService().execute(() -> controller.onStationInfoAvailable(cookie));

        } else if (item.getItemId() == R.id.menu_remove)
        {
            MyHandler.getHandler().post(() -> {
                HistoryFragmentView.HistoryListAdapter adapter =
                        (HistoryFragmentView.HistoryListAdapter) getListAdapter();

                if (adapter != null)
                    adapter.remove(info.position);
            });

            MyThreadPool.INSTANCE.getExecutorService().execute(() -> HistoryFragmentModel.updateHistoryList(info.position));

        } else if (item.getItemId() == R.id.menu_clear_history)
        {
            MyHandler.getHandler().post(() -> {
                HistoryFragmentView.HistoryListAdapter adapter =
                        (HistoryFragmentView.HistoryListAdapter) getListAdapter();

                if (adapter != null)
                    adapter.removeAll();

                HistoryManager.clearAll();
            });

            Toast.makeText(getActivity(), HISTORY_CLEARED, Toast.LENGTH_LONG).show();
        } else
        {
            return false;
        }

        return true;
    }

    private void displayHistory()
    {
        if (list != null) return;

        MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
            list = HistoryFragmentModel.getAllHistory();
            cancelProgress();

            if (list == null || list.size() < 1)
                list.add(new StationCookie(getString(R.string.empty_list)));

            historyFragmentView.showHistory(list);
        });
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id)
    {
        StationCookie station = (StationCookie) getListView().getItemAtPosition(position);
        if (checkIfEmpty(station)) return;

        MyThreadPool.INSTANCE.getExecutorService().execute(() -> controller.onStationInfoAvailable(station));
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

    private boolean checkIfEmpty(StationCookie entry)
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
        } else
        {
            throw new RuntimeException(context
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

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

        controller = null;
        historyFragmentView = null;
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
}
