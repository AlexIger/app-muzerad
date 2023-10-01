package com.cyberia.radio.radiodetails;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.cyberia.radio.AppRadio;
import com.cyberia.radio.R;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.interfaces.MvcView;
import com.cyberia.radio.interfaces.MvcViewEventListener;
import com.cyberia.radio.model.StationCookie;
import com.cyberia.radio.utils.StringCapitalizer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RadioDetailFragmentView implements MvcView
{
    public RadioDetailFragment presenter;
    private final View rootView;
    private MvcViewEventListener listener;
    private final Context context;


    public RadioDetailFragmentView(RadioDetailFragment _presenter, LayoutInflater _inflater, ViewGroup _container, Context con)
    {
        presenter = _presenter;
        context = con;
        rootView = _inflater.inflate(R.layout.fragment_radio_detail, _container, false);
    }

    public void postRadioDetails(ArrayList<String> detailsList)
    {
        MyHandler.getHandler().post(() -> {
            ImageView imgStationLogo = rootView.findViewById(R.id.imgRadioDetail);
            String s = detailsList.get(RadioDetailFragment.RadioInfo.URL_THUMB);

            Uri uri;

            if (TextUtils.isEmpty(s))
                uri = Uri.EMPTY;
            else
                uri = Uri.parse(s);

            Picasso.get()
                    .load(uri)
                    .resize(200, 200)
                    .centerInside()
                    .placeholder(!AppRadio.isNightMode ? R.drawable.ic_placeholder : R.drawable.ic_placeholder_night)
                    .error( !AppRadio.isNightMode ? R.drawable.ic_headset : R.drawable.hc_headset_white)
                    .into(imgStationLogo);

//                Picasso.get()
//                        .load(uri)
//                        .resize(200, 200)
//                        .centerInside()
//                        .placeholder(AppRadio.isNightMode == false ? R.drawable.ic_placeholder : R.drawable.ic_placeholder_night)
//                        .error(AppRadio.isNightMode == false ? R.drawable.ic_headset : R.drawable.hc_headset_white)
//                        .into(new Target()
//                        {
//                            @Override
//                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
//                            {
//                                imgStationLogo.setImageBitmap(bitmap);
//                            }
//
//                            @Override
//                            public void onBitmapFailed(Exception e, Drawable errorDrawable)
//                            {
//                                MyPrint.printOut("RadioDetail", e.getMessage());
//                            }
//
//                            @Override
//                            public void onPrepareLoad(Drawable placeHolderDrawable)
//                            {
//                                MyPrint.printOut("RadioDetail", "Radio detail image ready to load");
//                            }
//                        });


            TextView text = rootView.findViewById(R.id.lblStation);
            s = detailsList.get(RadioDetailFragment.RadioInfo.STATION);
            if (!TextUtils.isEmpty(s))
                text.setText(StringCapitalizer.capitalize(s));

            text = rootView.findViewById(R.id.lblGenreText);
            s = detailsList.get(RadioDetailFragment.RadioInfo.GENRE);
            if (!TextUtils.isEmpty(s))
                text.setText(StringCapitalizer.capitalize(s));

            text = rootView.findViewById(R.id.lblLangText);
            s = detailsList.get(RadioDetailFragment.RadioInfo.LANG);
            if (!TextUtils.isEmpty(s))
                text.setText(StringCapitalizer.capitalize(s));

            text = rootView.findViewById(R.id.lblCountryText);
            s = detailsList.get(RadioDetailFragment.RadioInfo.COUNTRY);
            if (!TextUtils.isEmpty(s))
                text.setText(StringCapitalizer.capitalize(s));

            text = rootView.findViewById(R.id.lblCodecText);
            s = detailsList.get(RadioDetailFragment.RadioInfo.CODEC);
            if (!TextUtils.isEmpty(s))
                text.setText(StringCapitalizer.capitalize(s));

            text = rootView.findViewById(R.id.lblBitrateText);
            s = detailsList.get(RadioDetailFragment.RadioInfo.BITRATE);
            if (!TextUtils.isEmpty(s))
                text.setText(s);

            text = rootView.findViewById(R.id.lblHomepage);
            s = detailsList.get(RadioDetailFragment.RadioInfo.HOMEPAGE);

            MyPrint.printOut("HTML", s);
            if (!TextUtils.isEmpty(s))
            {
                if (StationCookie.NOT_AVAIL_STRING.equalsIgnoreCase(s))
                {
                    text.setText(StationCookie.NOT_AVAIL_STRING);
                } else
                {
                    text.setText(HtmlCompat.fromHtml(
                            "<a href=\"" + s + "\">" +
                                    context.getString(R.string.link_homepage) + "</a>",
                            HtmlCompat.FROM_HTML_MODE_COMPACT));

                    text.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            ImageView homepage = rootView.findViewById(R.id.imgHomepage);
            homepage.setImageResource(R.drawable.ic_outline_home_24);
        });
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


}
