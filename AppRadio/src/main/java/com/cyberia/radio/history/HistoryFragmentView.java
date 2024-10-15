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
import androidx.fragment.app.ListFragment;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.R;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.persistent.Station;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HistoryFragmentView implements MvcView
{
    private final View rootView;
    private MvcViewEventListener listener;
    private final LayoutInflater inflater;
    public ListFragment presenter;
    private final Context context;

    //public constructor
    public HistoryFragmentView(ListFragment _presenter, LayoutInflater _inflater,
                               ViewGroup container, Context _context)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = _context;

        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    public void showHistory(final List<Station> list)
    {
       ViewListAdapter adapter = new ViewListAdapter(context, R.layout.row_station, list);
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
   public class ViewListAdapter extends ArrayAdapter<Station>
    {
        class ViewHolder
        {
            TextView fieldStation;
            ImageView stationImg;
        }

        private ViewHolder viewHolder;
        private final int layout;
        private final List<Station> listHistory;


        public ViewListAdapter(Context context, int resource, List<Station> listHistory)
        {
            super(context, resource, listHistory);
            layout = resource;
            this.listHistory = listHistory;
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

            Station station = getItem(position);
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
