package com.cyberia.radio.genres;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.collection.SimpleArrayMap;
import com.cyberia.radio.R;
import com.cyberia.radio.constant.GenreFlags;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GenresModel
{
    private GenreFragment presenter;

    Context context;

    public GenresModel()
    {
    }

    public GenresModel(GenreFragment presenter)
    {
        this.presenter = presenter;
        context = presenter.getContext();
    }

    public void readGenres(final int source)
    {
           MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
               try
               {
                   List<String> list = new ArrayList<>();

                   if (source == GenreFlags.MUSIC)
                   {
                       String[] typeMusic = context.getResources().getStringArray(R.array.sub_music);
                       list.addAll(Arrays.asList(typeMusic));
                       presenter.onGenresAvailable(list, buildMusicCategories());
                   } else
                   {
                       String[] typeTalk = context.getResources().getStringArray(R.array.sub_talk);
                       list.addAll(Arrays.asList(typeTalk));
                       presenter.onGenresAvailable(list, buildTalkCategories());
                   }
               } catch (Exception e)
               {
                   ExceptionHandler.onException("Exception in GenresModel: ", e);
               }
           });
    }

    private SimpleArrayMap<Integer, Drawable> buildMusicCategories()
    {
        SimpleArrayMap<Integer, Drawable> genreTypes = new SimpleArrayMap<>();

        genreTypes.put(0, AppCompatResources.getDrawable(context, R.drawable.ic_alternative));
        genreTypes.put(1, AppCompatResources.getDrawable(context,R.drawable.ic_sub_harmonica));
        genreTypes.put(2, AppCompatResources.getDrawable(context,R.drawable.ic_chillout));
        genreTypes.put(3, AppCompatResources.getDrawable(context,R.drawable.ic_sub_christmas));
        genreTypes.put(4, AppCompatResources.getDrawable(context,R.drawable.ic_sub_harp)); //classical
        genreTypes.put(5, AppCompatResources.getDrawable(context,R.drawable.ic_sub_country));
        genreTypes.put(6, AppCompatResources.getDrawable(context,R.drawable.ic_sub_dance));
        genreTypes.put(7, AppCompatResources.getDrawable(context,R.drawable.ic_funk));
        genreTypes.put(8, AppCompatResources.getDrawable(context,R.drawable.ic_sub_jazz));
        genreTypes.put(9, AppCompatResources.getDrawable(context,R.drawable.ic_sub_latin));
        genreTypes.put(10, AppCompatResources.getDrawable(context,R.drawable.ic_sub_cocktail));
        genreTypes.put(11, AppCompatResources.getDrawable(context,R.drawable.ic_sub_oldies)); //oldies
        genreTypes.put(12, AppCompatResources.getDrawable(context,R.drawable.ic_sub_pop));
        genreTypes.put(13, AppCompatResources.getDrawable(context,R.drawable.ic_rythm_and_blues)); //R&B
        genreTypes.put(14, AppCompatResources.getDrawable(context,R.drawable.ic_sub_rap)); //rap
        genreTypes.put(15, AppCompatResources.getDrawable(context,R.drawable.ic_sub_reggae));
        genreTypes.put(16, AppCompatResources.getDrawable(context,R.drawable.ic_sub_rock));
        genreTypes.put(17, AppCompatResources.getDrawable(context,R.drawable.ic_sub_techno_1));


        return genreTypes;
    }

    private SimpleArrayMap<Integer, Drawable> buildTalkCategories()
    {
        SimpleArrayMap<Integer, Drawable> genreTypes = new SimpleArrayMap<>();

        genreTypes.put(0, AppCompatResources.getDrawable(context,R.drawable.ic_comedy_1));
        genreTypes.put(1, AppCompatResources.getDrawable(context,R.drawable.ic_general_talk));
        genreTypes.put(2, AppCompatResources.getDrawable(context,R.drawable.ic_news_report));
        genreTypes.put(3, AppCompatResources.getDrawable(context,R.drawable.ic_trophy));



        return genreTypes;
    }
}

