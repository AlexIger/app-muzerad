package com.cyberia.radio.country;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberia.radio.R;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;

import java.util.List;


public class CountriesFragmentView implements MvcView
{
    private final View rootView;
    private final CountriesFragment presenter;
    private final RecyclerView recyclerView;

    interface RecyclerViewClickListener
    {
        void onRecyclerItemClick(CountriesModel.Country country);
    }

    public CountriesFragmentView(CountriesFragment presenter, ViewGroup container)
    {
        this.presenter = presenter;

        rootView = presenter.getLayoutInflater().inflate(R.layout.fragment_countries, container, false);
        recyclerView = rootView.findViewById(R.id.view_countries);
        recyclerView.setLayoutManager(new LinearLayoutManager(presenter.getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(presenter.requireActivity(), DividerItemDecoration.VERTICAL));
    }


    public void showCountries(final List<CountriesModel.Country> list, RecyclerViewClickListener listener)
    {
        final CustomAdapter customAdapter = new CustomAdapter(list, listener);

        presenter.requireActivity().runOnUiThread(() -> recyclerView.setAdapter(customAdapter));
    }

    public RecyclerView getRecyclerView()
    {
        return recyclerView;
    }

    protected void setListener(MvcViewEventListener listener)
    {
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
    private static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>
    {
        final List<CountriesModel.Country> countries;
        final RecyclerViewClickListener listener;
        final  String sourcePartOne = "file:///android_asset/country_flags/";
        final  String sourcePartTwo = ".png";
        String source;


        public CustomAdapter(List<CountriesModel.Country> listCountries, RecyclerViewClickListener listener)
        {
            countries = listCountries;
            this.listener = listener;
        }

        @Override
        @NonNull
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_country, parent, false);
            return new MyViewHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
        {
            holder.countryNameView.setText(countries.get(position).name);
            holder.stationCountView.setText(countries.get(position).getStationCount());
            String code = countries.get(position).code;
            source = sourcePartOne + code + sourcePartTwo;
            holder.imgViewFlag.setImageBitmap(countries.get(position).icon);


//            Picasso.get()
//                    .load(source)
//                    .resize(100, 100)
//                    .centerInside()
//                    .error(R.drawable.ic_placeholder)
//                    .noPlaceholder()
//                    .into(holder.imgViewFlag);
        }

        @Override
        public int getItemCount()
        {
            return countries.size();
        }

        private final class MyViewHolder extends RecyclerView.ViewHolder
        {
            private final TextView countryNameView;
            private final TextView stationCountView;
            private final ImageView imgViewFlag;

            public MyViewHolder(View row)
            {
                super(row);

                countryNameView = row.findViewById(R.id.country_title);
                stationCountView = row.findViewById(R.id.station_count);
                imgViewFlag = row.findViewById(R.id.country_image_view);


                row.setOnClickListener(v -> {
                    if (listener != null)
                    {
                        int position = getAbsoluteAdapterPosition();  //change 19/7/22
//                            if (position != RecyclerView.NO_POSITION)
//                                listener.onRecyclerItemClick(row, countryCode, position);

                        if (position != RecyclerView.NO_POSITION)
                        {
                            listener.onRecyclerItemClick(countries.get(position));
                        }
                    }
                });
            }
        }
    }
}
