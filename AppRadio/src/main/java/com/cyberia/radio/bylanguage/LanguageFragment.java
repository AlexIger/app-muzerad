package com.cyberia.radio.bylanguage;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.helpers.MyNavController;
import com.cyberia.radio.interfaces.Controller;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.sun.istack.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LanguageFragment extends ListFragment implements MvcViewEventListener
{
    private LanguageFragmentView langFragmentView;
    private LanguageFragmentModel model;
    private Controller controller;
    private ProgressBar progress;
    private Parcelable state;

    public LanguageFragment()
    {
    }

    public static LanguageFragment newInstance()
    {

        return new LanguageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView;

        if (langFragmentView == null)
        {
            langFragmentView = new LanguageFragmentView(this, inflater, container, getContext());
            langFragmentView.setListener(this);

            rootView = langFragmentView.getRootView();
            progress = rootView.findViewById(R.id.progressLanguage);
        }

        rootView = langFragmentView.getRootView();

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (model == null)
        {
            // MyPrint.printOut(getClass().getSimpleName(), "Language model is null");
            model = new LanguageFragmentModel(this);
            model.readLanguages();
        }

        controller.updateAppBarTitle(GenreFlags.CAT_BYLANG, null, true);
        MyNavController.setCategory(GenreFlags.CAT_BYLANG);
    }

    public void onListAvailable(Map<String, Integer> langMap)
    {
        cancelProgress();

        if (langMap == null || langMap.isEmpty())
        {
            MyHandler.post(() -> Toast.makeText(getActivity(), "Stations not found", Toast.LENGTH_LONG).show());
        } else
        {
            List<Map.Entry<String, Integer>> languageList = new ArrayList<>(langMap.entrySet());
            langFragmentView.showList(languageList);
        }
    }

    public void onModelError(String msg)
    {
        cancelProgress();
        MyHandler.post(() -> Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show());
    }


    @Override
    public void onListItemClick(@NonNull ListView listView, View view, int position, long id)
    {
       LanguageFragmentView.LanguageListAdapter.ViewHolder holder =
               (LanguageFragmentView.LanguageListAdapter.ViewHolder) view.getTag();

        controller.addStationScreenFragment((String) holder.fieldLang.getText(), GenreFlags.LANGUAGE);
    }

    private void cancelProgress()
    {
       requireActivity().runOnUiThread(() -> {
           if (progress != null)
           {
               progress.setVisibility(ProgressBar.GONE);
           }
       });
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
        // MyPrint.printOut(getClass().getSimpleName(), " onResume");
        super.onResume();

        if (state != null)
            getListView().onRestoreInstanceState(state);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        controller = null;
        langFragmentView = null;
        model = null;
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
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
        }
    }
}