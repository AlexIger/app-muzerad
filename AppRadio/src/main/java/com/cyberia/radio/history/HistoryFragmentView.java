package com.cyberia.radio.history;

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
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HistoryFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;
    private final LayoutInflater inflater;
    public HistoryFragment presenter;
    private final Context context;
//    private volatile HistoryListAdapter adapter;

    //public constructor
    public HistoryFragmentView(HistoryFragment _presenter, LayoutInflater _inflater,
                               ViewGroup container, Context _context)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = _context;

        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    public void showHistory(final List<StationCookie> list)
    {
        HistoryListAdapter adapter = new HistoryListAdapter(context, R.layout.row_station, list);
        presenter.requireActivity().runOnUiThread(() -> presenter.setListAdapter(adapter));
    }


    protected void setListener(MvcViewEventListener _listener)
    {
        listener = _listener;
    }

//    public HistoryListAdapter getAdapter()
//    {
//        return adapter;
//    }

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
    class HistoryListAdapter extends ArrayAdapter<StationCookie>
    {
        class ViewHolder
        {
            TextView fieldStation;
            ImageView stationImg;
        }

        private ViewHolder viewHolder;
        private final int layout;
        private final List<StationCookie> listHistory;


        public HistoryListAdapter(Context context, int resource, List<StationCookie> listHistory)
        {
            super(context, resource, listHistory);
            layout = resource;
            this.listHistory = listHistory;
        }

        public void remove(int position)
        {
            listHistory.remove(position);
            invalidate();
        }

        public void removeAll()
        {
            listHistory.clear();
            invalidate();
        }

        public void invalidate()
        {
            MyHandler.getHandler().post(() -> notifyDataSetChanged());
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = inflater.inflate(layout, parent, false); //change 19/7/22
                viewHolder = new ViewHolder();
                viewHolder.fieldStation = convertView.findViewById(R.id.station_genre);
                viewHolder.stationImg = convertView.findViewById(R.id.station_image);
                convertView.setTag(viewHolder);
            }
                viewHolder = (ViewHolder) convertView.getTag();

            StationCookie station = getItem(position);
            String imgUrl = station.getThumbUrl();
            String title = station.getTitle();

            viewHolder.fieldStation.setText(title);
            // lazyload thumbs
            Picasso.get()
                    .load(imgUrl)
                    .resize(100, 100)
                    .centerInside()
                    .placeholder(!AppRadio.isNightMode ? R.drawable.ic_headset : R.drawable.hc_headset_white)
                    .into(viewHolder.stationImg);

            return convertView;
        }
    }
}
