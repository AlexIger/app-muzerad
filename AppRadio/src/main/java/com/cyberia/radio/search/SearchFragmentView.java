package com.cyberia.radio.search;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.R;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.cyberia.radio.utils.StringCapitalizer;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;


public class SearchFragmentView implements MvcView
{
    //    private final static String TAG = "SearchFragmentView";
    private final View rootView;
    private MvcViewEventListener listener;
    private final LayoutInflater inflater;
    public SearchFragment presenter;
    private final Context context;


    public SearchFragmentView(SearchFragment _presenter, LayoutInflater _inflater, ViewGroup _container, Context con)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = con;

        rootView = inflater.inflate(R.layout.fragment_search, _container, false);
    }

    public void showStations(final List<StationCookie> stations)
    {
        StationsListAdapter listAdapter = new StationsListAdapter(stations, R.layout.row_main);

        presenter.requireActivity().runOnUiThread(() -> {
            presenter.setListAdapter(listAdapter);
            presenter.initialFilter();
        });
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
    class StationsListAdapter extends ArrayAdapter<StationCookie> implements Filterable
    {
        class ViewHolder
        {
            TextView fieldStationTitle;
            TextView fieldStationGenre;
            ImageView stationImg;
        }

        private final Object lock = new Object();
        private volatile List<StationCookie> list;
        private final List<StationCookie> originalList;
        private final int resource;
        private ViewHolder viewHolder;



        public StationsListAdapter(List<StationCookie> list, int resource)
        {
            super(context, resource, list);

            this.resource = resource;
            this.list = list;
            originalList = new ArrayList<>(list);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            if (convertView == null)
            {
                viewHolder = new ViewHolder(); //change 19/7/22
                // create a layout view for a station row / record
                convertView = inflater.inflate(resource, parent, false);

                // get the fields: text field and image viewer
                viewHolder.fieldStationTitle = convertView.findViewById(R.id.station_title_const);
                viewHolder.fieldStationGenre = convertView.findViewById(R.id.station_genre_const);
                viewHolder.stationImg = convertView.findViewById(R.id.station_image_const);

                convertView.setTag(viewHolder);
            }
                viewHolder = (ViewHolder) convertView.getTag();

            // get the the object in a list
            StationCookie cookie = getItem(position);
            String imgUrl = cookie.getThumbUrl();
            String stationTitle = cookie.getTitle();
            String stationGenre = cookie.getGenre();

            // fill out the row /record with the name of the station
            viewHolder.fieldStationTitle.setText(stationTitle);
            viewHolder.fieldStationGenre.setText(StringCapitalizer.capitalizeBlanks(stationGenre));
            viewHolder.fieldStationTitle.setEllipsize(TextUtils.TruncateAt.END);
            viewHolder.fieldStationGenre.setEllipsize(TextUtils.TruncateAt.END);

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
//                    .error(R.drawable.ic_headset)
                    .placeholder(!AppRadio.isNightMode ? R.drawable.ic_headset : R.drawable.hc_headset_white)
                    .into(viewHolder.stationImg);

            return convertView;
        }

        public void clearData()
        {
            list.clear();
            originalList.clear();
        }


        @NonNull
        @Override
        public Filter getFilter()
        {
            return new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence)
                {
                    List<StationCookie> filteredList;

                    String charString = charSequence.toString();

                    synchronized (lock)
                    {
                        filteredList = new ArrayList<>();
                        for (StationCookie cookie : originalList)
                        {
                            if (cookie.getTitle().toLowerCase().contains(charString.toLowerCase()))
                            {
                                filteredList.add(cookie);
                            }
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredList;
                    return filterResults;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults)
                {
                    list = (ArrayList<StationCookie>) filterResults.values;
                    if (list != null || !list.isEmpty())
                    {
                        clear();
                        addAll(list);
                        notifyDataSetChanged();
                    }
                }
            };
        }
    }
}

