package com.cyberia.radio.bylanguage;

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
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;

import java.util.List;
import java.util.Map;

public class LanguageFragmentView implements MvcView
{
    private final View rootView;
    private LayoutInflater inflater;
    public LanguageFragment presenter;
    private final Context context;


    public LanguageFragmentView(LanguageFragment _presenter, LayoutInflater _inflater, ViewGroup container, Context con)
    {
        presenter = _presenter;
        inflater = _inflater;
        context = con;
        rootView = inflater.inflate(R.layout.fragment_language, container, false);
    }

    public void showList(List<Map.Entry<String, Integer>> langs)
    {
        LanguageListAdapter listAdapter = new LanguageListAdapter(langs, R.layout.row_country);

        presenter.requireActivity().runOnUiThread(() -> presenter.setListAdapter(listAdapter));
    }

    protected void setListener(MvcViewEventListener _listener)
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
    class LanguageListAdapter extends ArrayAdapter<Map.Entry<String, Integer>>
    {
        class ViewHolder
        {
            TextView fieldLang;
            TextView fieldCount;
            ImageView fieldImg;
        }

        int resource;
        private ViewHolder viewHolder;

        public LanguageListAdapter(List<Map.Entry<String, Integer>> listLang, int resource)
        {
            super(context, resource, listLang);

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.resource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = inflater.inflate(resource, parent, false);
                viewHolder = new ViewHolder();
                //get the text field in the row
                viewHolder.fieldLang = convertView.findViewById(R.id.country_title);
                //get the image field in the row
                viewHolder.fieldImg = convertView.findViewById(R.id.country_image_view);
                //store Viewholder to the row
                viewHolder.fieldCount = convertView.findViewById(R.id.station_count);

                convertView.setTag(viewHolder);
            }
                viewHolder = (ViewHolder) convertView.getTag();


            Map.Entry<String, Integer> element = getItem(position);

            viewHolder.fieldLang.setText(element.getKey());
            viewHolder.fieldCount.setText(String.valueOf(element.getValue()));
            viewHolder.fieldImg.setImageResource(R.drawable.ic_language_3);

            return convertView;
        }
    }
}
