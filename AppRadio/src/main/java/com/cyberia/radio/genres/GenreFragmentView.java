package com.cyberia.radio.genres;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.cyberia.radio.R;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;

import java.util.List;


public class GenreFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;
    public GenreFragment presenter;


    public GenreFragmentView(GenreFragment _presenter, LayoutInflater inflater, ViewGroup container)
    {
        presenter = _presenter;
        rootView = inflater.inflate(R.layout.fragment_genres, container, false);
    }

    public void showGenres(Context con, List<String> list, SimpleArrayMap<Integer, Drawable> genreTypes)
    {
        GenreListAdapter myAdapter = new GenreListAdapter(con, R.layout.row_genre, list, genreTypes);

        MyHandler.post(() -> presenter.setListAdapter(myAdapter));
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


    //-----------------------------------/ Custom list adapter inner class /----------------------------
    private static class GenreListAdapter extends ArrayAdapter<String>
    {
        static class ViewHolder
        {
            TextView fieldGenre;
            ImageView stationImg;
        }

        int layout;
        LayoutInflater inflater;
        private ViewHolder viewHolder;

        String genreTitle;
        SimpleArrayMap<Integer, Drawable> genTypes;


        public GenreListAdapter(Context context, int resource, List<String> listGenres, SimpleArrayMap<Integer, Drawable> genreTypes)
        {
            super(context, resource, listGenres);
            layout = resource;
            genTypes = genreTypes;
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {

            if (convertView == null)
            {
                convertView = inflater.inflate(layout, parent, false);

                viewHolder = new ViewHolder();

                //get the text viewer in the row
                viewHolder.fieldGenre = convertView.findViewById(R.id.genre_title);
                //get the image viewer in the row
                viewHolder.stationImg = convertView.findViewById(R.id.genre_image);

                //bind Viewholder to the row
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();

            //get the next oblect in the List
            genreTitle = getItem(position);
            //set this textview's text and store the textview in the Viewholder
            viewHolder.fieldGenre.setText(genreTitle);
            //set icon image
//            viewHolder.stationImg.setImageResource(R.drawable.ic_equalizer_24px)
            if (genTypes != null && !genTypes.isEmpty())
                viewHolder.stationImg.setImageDrawable(genTypes.get(position));
            else
                viewHolder.stationImg.setImageResource(R.drawable.ic_headset);

            return convertView;
        }
    }
}