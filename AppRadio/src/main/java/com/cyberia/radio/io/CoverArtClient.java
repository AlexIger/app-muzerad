package com.cyberia.radio.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.R;
import com.cyberia.radio.coverartresponse.CoverArtResponse;
import com.cyberia.radio.coverartresponse.ImagesItem;
import com.cyberia.radio.coverartresponse.Thumbnails;
import com.cyberia.radio.global.MyHandler;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.Queries;
import com.cyberia.radio.model.StationCookie;
import com.cyberia.radio.musicbrainzresponse.MusicBrainzResponse;
import com.cyberia.radio.musicbrainzresponse.RecordingsItem;
import com.cyberia.radio.musicbrainzresponse.ReleasesItem;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoverArtClient
{
    private final int REQUEST_MUSICBRAINZ = 0;
    private final int REQUEST_COVERART = 1;
    private final int CONN_TIMEOUT = 5000;
    private final int READ_TIMEOUT = 5000;
    private static final String TAG = "CoverArtClient";
    private final static String coverArtUrL = "http://coverartarchive.org/release/";
    private static CoverArtClient instance;
    public final static String TAG_DEFAULT_LOGO = "logo_default";
    public final static String TAG_COVER_ART = "cover_art";
    private MainActivity controller;
    private final CoverArtConnectionListener listener = new CoverArtConnectionListener();


    public static CoverArtClient getInstance()
    {
        if (instance == null)
        {
            instance = new CoverArtClient();
        }
        return instance;
    }

    public CoverArtClient setController(MainActivity main)
    {
        controller = main;
        return this;
    }

    public synchronized void showDefaultLogo()
    {
        //        MyPrint.printOut("Cover art", "attempting to retrieve default logo");
        CountDownLatch latch = new CountDownLatch(1);
        ImageView view = controller.findViewById(R.id.cover_art_image_view);

        MyThreadPool.INSTANCE.getExecutorService().execute(new Runnable()
        {
            Bitmap imageLogo;

            @Override
            public void run()
            {
                try
                {
                    if (controller == null)
                        throw new CoverArtException("Controller is null");

                    StationCookie cookie = controller.getCookieRefs();
                    String thumbnailUrl = (cookie == null) ? null : cookie.getThumbUrl();

                    if (TextUtils.isEmpty(thumbnailUrl))
                        throw new CoverArtException("Thumb URL is empty");

                    OkHttpClient client = ConnectionClient.OkSingleton.getInstance().getClient();

                    client.newBuilder()
                            .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .eventListener(listener)
                            .build();

                    Request request = new Request.Builder()
                            .url(thumbnailUrl)
                            .tag(TAG_DEFAULT_LOGO)
                            .build();

                    Call call = client.newCall(request);

                    try (Response response = call.execute())
                    {
                        if (response.isSuccessful())
                        {
                            InputStream inputStream;

                            inputStream = response.body().byteStream();

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inScaled = false;
                            imageLogo = BitmapFactory.decodeStream(inputStream, null, options);
                        }
                    }
                    catch (IOException e)
                    {
                        ExceptionHandler.onException(TAG, 122, e);
                    }
                }
                catch (CoverArtException e)
                {
                    ExceptionHandler.onException(TAG, 126, e);
                }

                MyHandler.post(() ->
                {
                    if (Objects.nonNull(imageLogo))
                        view.setImageBitmap(imageLogo);
                    else
                        view.setImageResource(R.drawable.ic_no_photo);
                });

                latch.countDown();
            }
        });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            ExceptionHandler.onException(TAG, 150, e);
        }
    }

    public void fetchCoverArt(String artistSong)
    {
        try
        {
            if (artistSong == null || artistSong.trim().length() < 3)
                throw new CoverArtException("Artist-Song string is null/empty");

            //prepare the artist and song strings, or pass null
            //            Pair<String, String> artistSong = SongTitleFilter.filterArtistSong(nowPlaying);

            String[] pair = artistSong.split("-");
            String mbString = null;

            if (pair.length > 1)
                mbString = getCoverArtUrl(Queries.getMBIDQuery(pair[0], pair[1]), REQUEST_MUSICBRAINZ);

            if (TextUtils.isEmpty(mbString))
                throw new CoverArtException("From MusicBrainz: empty response");

            String imgUrl = getCoverArtUrl(coverArtUrL + mbString, REQUEST_COVERART);
            // MyPrint.printOut(TAG, "Returned imgUrl: " + imgUrl);

            if (TextUtils.isEmpty(imgUrl))
                throw new CoverArtException("From CoverArt: empty response");

            displayImage(imgUrl);

        }
        catch (CoverArtException e)
        {
            ExceptionHandler.onException(TAG, 256, e);
            showDefaultLogo();
        }
    }

    private void displayImage(String imageUrl)
    {
        final CountDownLatch latch = new CountDownLatch(1);

        OkHttpClient client = ConnectionClient.OkSingleton.getInstance().getClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .eventListener(listener)
                .build();

        Request request = new Request.Builder()
                .url(imageUrl)
                .tag(TAG_COVER_ART)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                // MyPrint.printOut(TAG, "onFailure: DisplayImage failed, displaying default logo");
                ExceptionHandler.onException(TAG, 214, e);
                showDefaultLogo();
                latch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException
            {
                if (call.isCanceled())
                {
                    // MyPrint.print(TAG, "DisplayImage #onResponse: Call was canceled", 224);
                    throw new IOException("Call was canceled");
                }
                else if (!response.isSuccessful())
                {
                    // MyPrint.printOut(TAG, "DisplayImage #onResponse: Unsuccessful, attempt to display default logo");
                    showDefaultLogo();
                }
                else
                {
                    // MyPrint.printOut(TAG, "DisplayImage #onResponse: Success, attempt to display Art Image");
                    final ImageView view = controller.findViewById(R.id.cover_art_image_view);

                    InputStream inputStream;

                    inputStream = response.body().byteStream();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;
                    Bitmap imgCoverArt = BitmapFactory.decodeStream(inputStream);

                    MyHandler.post(() ->
                    {
                        if (imgCoverArt != null)
                            view.setImageBitmap(imgCoverArt);
                        else
                            showDefaultLogo();
                    });
                }
                latch.countDown();
            }
        });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            ExceptionHandler.onException(TAG, 257, e);
        }
    }

    private String getCoverArtUrl(String urlAddress, int flag)
    {
        // MyPrint.printOut(TAG, "URL address: " + urlAddress);

        final String[] responseString = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);

        OkHttpClient client = ConnectionClient.OkSingleton.getInstance().getClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .eventListener(listener)
                .build();

        // String query1 = "http://www.musicbrainz.org/ws/2/recording/?query=artist:Pink+Floyd+recording:On+the+Run&limit=1&fmt=json";
        String USER_AGENT = "Android/13.0";
        Request request = new Request.Builder()
                .header("user-agent", USER_AGENT)
                .url(urlAddress)
                .tag(TAG_COVER_ART)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                // MyPrint.printOut(TAG, "GetCoverArtUrl; onFailure returning responseString = null");
                ExceptionHandler.onException(TAG, 285, e);
                responseString[0] = null;
                latch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException
            {
                if (call.isCanceled())
                {
                    // MyPrint.print(TAG, "GetCoverArtUrl; Call was canceled", 295);
                    throw new IOException("Call was canceled");
                }
                else if (!response.isSuccessful())
                {
                    // MyPrint.print(TAG, "GetCoverArtUrl; response not successful", 300);
                    responseString[0] = null;
                }
                else
                {
                    try
                    {
                        Gson gson = new Gson();

                        switch (flag)
                        {
                            case REQUEST_MUSICBRAINZ ->
                            {
                                String jsonStringMB = response.body().string();
                                MusicBrainzResponse mbResponse = gson.fromJson(jsonStringMB, MusicBrainzResponse.class);
                                List<RecordingsItem> recordingItemList;
                                RecordingsItem recordingsItem;
                                List<ReleasesItem> releasesItems;
                                ReleasesItem releasesItem;
                                if (!CollectionUtils.isEmpty(recordingItemList = mbResponse.getRecordings()) &&
                                        (recordingsItem = recordingItemList.get(0)) != null &&
                                        (!CollectionUtils.isEmpty(releasesItems = recordingsItem.getReleases()) &&
                                                (releasesItem = releasesItems.get(0)) != null))
                                    responseString[0] = releasesItem.getId();
                            }
                            case REQUEST_COVERART ->
                            {
                                String jsonStringCoverArt = response.body().string();
                                CoverArtResponse artResponse = gson.fromJson(jsonStringCoverArt, CoverArtResponse.class);
                                List<ImagesItem> imagesItemList;
                                ImagesItem imagesItem;
                                Thumbnails thumbnails;
                                if (!CollectionUtils.isEmpty(imagesItemList = artResponse.getImages()) &&
                                        (imagesItem = imagesItemList.get(0)) != null &&
                                        (thumbnails = imagesItem.getThumbnails()) != null)
                                    responseString[0] = thumbnails.getLarge();
                            }
                        }
                    }
                    catch (JsonSyntaxException e)
                    {
                        ExceptionHandler.onException(TAG, 347, e);
                        responseString[0] = null;
                    }
                }

                latch.countDown();
            }
        });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            ExceptionHandler.onException(TAG, 348, e);
        }

        return responseString[0];
    }

    static class CoverArtException extends Exception
    {
        CoverArtException(String exeptionString)
        {
            super("CoverArtException: " + exeptionString);
        }
    }

    public synchronized CoverArtClient cancelCallWithTag(String tag)
    {
        OkHttpClient currentClient = ConnectionClient.OkSingleton.getInstance().getClient();

        for (Call call : currentClient.dispatcher().queuedCalls())
        {
            if (tag.equals(call.request().tag()))
                call.cancel();
        }
        for (Call call : currentClient.dispatcher().runningCalls())
        {
            if (tag.equals(call.request().tag()))
                call.cancel();
        }

        return this;
    }

    static class CoverArtConnectionListener extends EventListener
    {
        @Override
        public void callFailed(@NonNull Call call, @NonNull IOException ioe)
        {
            ExceptionHandler.onException(TAG, 386, ioe);
        }
    }
}
