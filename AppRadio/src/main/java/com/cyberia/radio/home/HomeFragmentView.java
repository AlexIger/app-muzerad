package com.cyberia.radio.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;


public class HomeFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;


    static final Integer[] imgID =
            {
                    R.drawable.ic_favorites,
                    R.drawable.ic_cat_trending,
                    R.drawable.ic_cat_music,
                    R.drawable.ic_cat_talk_1,
                    R.drawable.ic_cat_bycountry,
                    R.drawable.ic_lang_5,
                    R.drawable.ic_time_back,
                    R.drawable.ic_magnifying_glass,
                    R.drawable.ic_staiton_playlist

            };


    //public constructor
    public HomeFragmentView(HomeFragment presenter, LayoutInflater inflater, ViewGroup _container)
    {
        presenter.setListAdapter(new HomeFragmentAdapter
                (presenter.getContext(), inflater, GenreFlags.RadioCategories, imgID));

        //        presenter.setListAdapter(new HomeFragmentAdapter(presenter.getContext(), inflater, StationCategory.RadioCategories, imgID));
        rootView = inflater.inflate(R.layout.fragment_home, _container, false);
    }


    protected void setListener(MvcViewEventListener _listener)
    {
        listener = _listener;
    }


    @Override
    public View getRootView()
    {
        return rootView;
    }

    @Override
    public Bundle getViewState()
    {
        return null;
    }


    // -----------------------------------------------/ Custom adapter inner class /--------------------
    static class HomeFragmentAdapter extends ArrayAdapter<String>
    {
        private final String[] itemName;
        private final Integer[] imgID;
        private final LayoutInflater inflater;

        public HomeFragmentAdapter(Context cont, LayoutInflater _inflater, String[] _itemName, Integer[] _imgID)
        {
            super(cont, R.layout.row_home_item, _itemName);

            inflater = _inflater;
            itemName = _itemName;
            imgID = _imgID;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            @SuppressLint("ViewHolder")
            View rowField = inflater.inflate(R.layout.row_home_item, parent, false);

            TextView txtTitle = rowField.findViewById(R.id.item_name);
            ImageView imageView = rowField.findViewById(R.id.icon);

            txtTitle.setText(itemName[position]);
            imageView.setImageResource(imgID[position]);
            rowField.setTag(itemName[position]);

            return rowField;
        }
    }

}