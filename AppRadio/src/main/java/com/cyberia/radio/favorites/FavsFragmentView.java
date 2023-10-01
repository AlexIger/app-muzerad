package com.cyberia.radio.favorites;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.R;
import com.cyberia.radio.db.Station;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.utils.StringCapitalizer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavsFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;
    private final LayoutInflater inflater;
    public FavsFragment presenter;
    private final Context context;


    //public constructor
    public FavsFragmentView(FavsFragment _presenter, LayoutInflater _inflater,
                            ViewGroup _container, Context _context)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = _context;

        rootView = inflater.inflate(R.layout.fragment_favorites, _container, false);
    }

    public void showFavs(final List<Station> list)
    {
        FavsListAdapter adapter = new FavsListAdapter(context, R.layout.row_main, list);

        presenter.requireActivity().runOnUiThread(() -> presenter.setListAdapter(adapter));
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
    class FavsListAdapter extends ArrayAdapter<Station>
    {
        class ViewHolder
        {
            TextView fieldStation;
            TextView fieldGenre;
            ImageView stationImg;
        }

        private final int layout;
        private final List<Station> list;
        private  ViewHolder viewHolder;

        public FavsListAdapter(Context context, int resource, List<Station> listOfFavs)
        {
            super(context, resource, listOfFavs);
            layout = resource;
            list = listOfFavs;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            // create a layout view for a station row / record
            if (convertView == null) //change 19/7/22
            {
                convertView = inflater.inflate(layout, parent, false);
                viewHolder = new ViewHolder();

                viewHolder.fieldStation = convertView.findViewById(R.id.station_title_const);
                viewHolder.fieldGenre = convertView.findViewById(R.id.station_genre_const);
                viewHolder.stationImg = convertView.findViewById(R.id.station_image_const);

                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();


            Station station = getItem(position);

            String imgUrl = station.thumbUrl;
            viewHolder.fieldStation.setText(station.title);
            viewHolder.fieldGenre.setText(StringCapitalizer.capitalizeBlanks(station.genre));

            // lazyload thumbs
            Picasso.get()
                    .load(imgUrl)
                    .resize(100, 100)
                    .centerInside()
                    .placeholder(!AppRadio.isNightMode ? R.drawable.ic_headset : R.drawable.hc_headset_white)
                    .into(viewHolder.stationImg);

            return convertView;
        }

        public void remove(int position)
        {
            list.remove(position);
            invalidate();
        }

        public void removeAll()
        {
            list.clear();
            invalidate();
        }

        public void invalidate()
        {
           MyHandler.post(() -> notifyDataSetChanged());
        }
    }
}
