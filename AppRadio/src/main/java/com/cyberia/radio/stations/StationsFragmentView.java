package com.cyberia.radio.stations;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.R;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.cyberia.radio.utils.StringCapitalizer;
import com.squareup.picasso.Picasso;

import java.util.List;


public class StationsFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;
    private final LayoutInflater inflater;
    public StationsFragment presenter;
    private final Context context;


    public StationsFragmentView(StationsFragment _presenter, LayoutInflater _inflater, ViewGroup container, Context con)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = con;

//        presenter.setListAdapter(new GenreListAdapter(presenter.getContext(), inflater, genres));
        rootView = inflater.inflate(R.layout.fragment_stations, container, false);
    }

    public void showStations(List<StationCookie> stations)
    {
        if (presenter.getListAdapter() == null)
        {
            final StationsListAdapter adapter = new StationsListAdapter(stations, R.layout.row_main);

            presenter.requireActivity().runOnUiThread(() -> presenter.setListAdapter(adapter));
        } else
        {
            final StationsListAdapter adapter = (StationsListAdapter) presenter.getListAdapter();
            presenter.requireActivity().runOnUiThread(() -> adapter.addToList(stations));
        }
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
    private class StationsListAdapter extends ArrayAdapter<StationCookie>
    {
        class ViewHolder
        {
            TextView fieldStationTitle;
            TextView fieldStationGenre;
            ImageView stationImg;
        }

        int resource;
        private ViewHolder viewHolder;


        public StationsListAdapter(List<StationCookie> list, int resource)
        {
            super(context, resource, list);
            this.resource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            if (convertView == null)
            {
                viewHolder = new ViewHolder();
                // create a layout view for a station row / record
                convertView = inflater.inflate(resource, parent, false);

                // get the fields: text field and image viewer
                viewHolder.fieldStationTitle = convertView.findViewById(R.id.station_title_const);
                viewHolder.fieldStationGenre = convertView.findViewById(R.id.station_genre_const);
                viewHolder.stationImg = convertView.findViewById(R.id.station_image_const);

                viewHolder.fieldStationTitle.setEllipsize(TextUtils.TruncateAt.END);
                viewHolder.fieldStationGenre.setEllipsize(TextUtils.TruncateAt.END);

                convertView.setTag(viewHolder);
            }
                viewHolder = (ViewHolder) convertView.getTag();

            // get the the object in a list
            StationCookie cookie = getItem(position);
            String imgUrl = cookie.getThumbUrl();
            String stationTitle = cookie.getTitle();
            String stationGenre = StringCapitalizer.capitalizeBlanks(cookie.getGenre());

            // fill out the row /record with the name of the station
            viewHolder.fieldStationTitle.setText(stationTitle);
            viewHolder.fieldStationGenre.setText(stationGenre);
//            viewHolder.fieldStationTitle.setEllipsize(TextUtils.TruncateAt.END);
//            viewHolder.fieldStationGenre.setEllipsize(TextUtils.TruncateAt.END);

//            stationImg.setImageResource(R.drawable.ic_headset);

//            Glide.with(context)
//                    .load(imgUrl)
//                    .override(100,100)
//                    .error(R.drawable.ic_headset) //error
//                    .placeholder(R.drawable.ic_headset)
//                    .centerInside()
//                    .into(holder.stationImg);


            Picasso.get()
                    .load(imgUrl)
                    .resize(100, 100)
                    .centerInside()
                    .placeholder(!AppRadio.isNightMode ? R.drawable.ic_headset : R.drawable.hc_headset_white)
                    .into(viewHolder.stationImg);

            return convertView;
        }

        private void addToList(List<StationCookie> stations)
        {
            try
            {
                addAll(stations);
                notifyDataSetChanged();
            } catch (Exception e)
            {
                ExceptionHandler.onException(getClass().getSimpleName(), e);
            }
        }
    }
}