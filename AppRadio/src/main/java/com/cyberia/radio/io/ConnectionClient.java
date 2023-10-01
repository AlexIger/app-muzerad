package com.cyberia.radio.io;

import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.cyberia.radio.MainActivity;
import com.cyberia.radio.eventbus.Events;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.global.MyThreadPool;
import com.cyberia.radio.helpers.ExceptionHandler;
import com.cyberia.radio.helpers.MyPrint;
import com.cyberia.radio.utils.CircularByteBuffer;
import com.google.android.exoplayer2.C;

import org.greenrobot.eventbus.EventBus;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

final public class ConnectionClient
{
    public interface ConnectionCallback
    {
        void onStreamAvailable(InputStream in);

        void onConnectionEstablished();
    }

    private static final String TAG = "ConnectionClient";
    private static final String TAG_D = "ConnectionClient#downloadTask";
    private final static String TAG_CONNECTION_CALL = "connection_call";
    // Message strings ******************************************************************
    public final static String msgConnLost = "Stream unavailable";
    public final static String msgStationUnavail = "Station unavailable";
    public final static String msgBuffering = "Buffering...";
    // ***********************************************************************************
    private volatile String urlAddress;
    private final int bufferSize;
    private final int connTimeout;
    // ***********************************************************************************
    private final AtomicBoolean stopBuffering = new AtomicBoolean(false);
    public final static String DEFAULT_CONN_TIMEOUT = "5000";
    private final static String PREF_DEFAULT_CONN_TIMEOUT = "conn_timeout";
    private ConnectionCallback conCallback;
    private Call call;

    final private AsyncExecutor threadExecutor = new AsyncExecutor();

    private static class AsyncExecutor implements Executor
    {
        @Override
        public void execute(Runnable r)
        {
            new Thread(r).start();
        }
    }

    private final Runnable startConn = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                getAudioStream((TextUtils.isEmpty(urlAddress)) ? MainActivity.STRING_EMPTY : urlAddress);
            } catch (IllegalArgumentException e)
            {
                EventBus.getDefault().post(new Events.FailEvent(msgStationUnavail));
                ExceptionHandler.onException(TAG, 141, e);
            } catch (Exception e)
            {
                onFailedConnection(msgStationUnavail);
                ExceptionHandler.onException(TAG, 147, e);
            }
        }
    };

    public static ConnectionClient newInstance(ConnectionClient.ConnectionCallback listener)
    {
        ConnectionClient client = new ConnectionClient();
        client.setConListener(listener);

        return client;
    }

    public ConnectionClient()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyAppContext.INSTANCE.getAppContext());
        String buffer = prefs.getString("buffer", String.valueOf(64000));
        bufferSize = Integer.parseInt(buffer);

        String timeout = prefs.getString(PREF_DEFAULT_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT);
        connTimeout = Integer.parseInt(timeout);
    }


    public void setConListener(ConnectionClient.ConnectionCallback listener)
    {
        conCallback = listener;
    }

    public void startSession(String url)
    {
        urlAddress = url;
       MyThreadPool.INSTANCE.getExecutorService().execute(startConn);
    }

    public synchronized void cancelCall()
    {
        if (call != null && TAG_CONNECTION_CALL.equals(call.request().tag()))
            call.cancel();
    }

    public synchronized boolean isCurrentCallCanceled()
    {
        return call.isCanceled();
    }

    public synchronized void stopSession()
    {
        stopBuffering.set(true);
    }

    public boolean isTrue()
    {
        return true;
    }


    private void getAudioStream(String url) throws IOException, IllegalArgumentException
    {
        final InputStream rawStream;

        OkHttpClient client = OkSingleton.getInstance().getClient().newBuilder()
                .connectTimeout(connTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .eventListener(new ConnectionListener())
                .build();

        String USER_AGENT = "Android/13.0";
        Request request = new Request.Builder()
                .header("user-agent", USER_AGENT)
                .header("Icy-MetaData", "1")
                .header("Connection", "Keep-Alive")
                .header("cache-control", "no-cache")
                .url(url)
                .tag(TAG_CONNECTION_CALL)
                .build();

        call = client.newCall(request);

        try
        {
            Response response = call.execute();

            if (response.body().contentType() == null)
                throw new MyConnectionException("Broadcast not available; content is null");

            if (!response.isSuccessful())
                throw new MyConnectionException("Cannot get a connection; response code: " + response.code());

            createServerInfo(response);

            rawStream = response.body().byteStream();

            fireConnectionStatusEvent(msgBuffering);
            buildNotification();
            startBuffers(rawStream);

        } catch (MyConnectionException e)
        {
            throw e;
        }
    }

    private void startBuffers(final InputStream in)
    {
        streamTask(in);
    }

    private void buildNotification()
    {
        conCallback.onConnectionEstablished();
    }

    private void onFailedConnection(final String msg)
    {
        if (call != null && !isCurrentCallCanceled())
            EventBus.getDefault().post(new Events.FailEvent(msg));
    }

    private void onBufferingComplete(final InputStream stream)
    {
       MyThreadPool.INSTANCE.getExecutorService().execute(() -> {
           //send stream to the player
           if (ServerInfo.getIcyMetaInt() == ServerInfo.NON_META_STREAM)
           {
               MyPrint.printOut(TAG, "Non ICY stream detected");
               conCallback.onStreamAvailable(stream);
           } else
           {
               conCallback.onStreamAvailable(MetaDataInputStream.getInstance(stream));
           }
       });
    }

    public void fireBufferingEvent(final int progress)
    {
        EventBus.getDefault().post(new Events.BufferingEvent(progress));
    }

    public void fireConnectionStatusEvent(final String status)
    {
        EventBus.getDefault().post(new Events.ConnectionStatusEvent(status));
    }

    private void streamTask(InputStream inputStream)
    {
        threadExecutor.execute(new Runnable()
        {
            final byte[] b = new byte[4096];
            final CircularByteBuffer cbb = new CircularByteBuffer(bufferSize * 2, true);
            final OutputStream out = cbb.getOutputStream();
            final InputStream rawStream = inputStream;

            final int stepIncrease = bufferSize / 10;
            int step = stepIncrease;
            boolean stopNotifying = false;
            int progress = 0;
            int available;
            int length;

            @Override
            public void run()
            {
                try
                {
                    while (!stopBuffering.get())
                    {
                        length = rawStream.read(b);

                        if (length == C.RESULT_END_OF_INPUT)
                            throw new MyConnectionException("End of stream reached; length = " + length);
                        else
                            out.write(b, 0, length);

                        if (!stopNotifying)
                        {
                            available = cbb.getAvailable();

                            if (available >= step)
                            {
                                progress++;
                                step = step + stepIncrease;
                                fireBufferingEvent(progress);
                            }
                            if (available >= bufferSize)
                            {
                                onBufferingComplete(cbb.getInputStream());
                                stopNotifying = true;
                            }
                        }
                    }
                } catch (MyConnectionException e)
                {
                    onFailedConnection(msgConnLost);
                    ExceptionHandler.onException(TAG_D, 324, e);
                } catch (IOException e)
                {
                    ExceptionHandler.onException(TAG_D, 327, e);
                } finally
                {
                    closeQuietly(cbb.getOutputStream());
                    closeQuietly(cbb.getInputStream());
                }
            }
        });
    }

    private void closeQuietly(Closeable stream)
    {
        try
        {
            if (stream != null)
                stream.close();
        } catch (IOException e)
        {
            MyPrint.printOut(TAG, "closeQuietly: " + e.getMessage());
        }
    }

    public void createServerInfo(Response response)
    {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.addItem(ServerInfo.ICY_META_INT, response.header("icy-metaint"));
        serverInfo.addItem(ServerInfo.ICY_GENRE, response.header("icy-genre"));
        serverInfo.addItem(ServerInfo.ICY_NAME, response.header("icy-name"));
    }

    /************************************************** Inner classes *****************************/

    public static class OkSingleton
    {
        final private OkHttpClient client;

        private static class Holder
        {
            private static final OkSingleton instance = new OkSingleton();
        }

        private OkSingleton()
        {
            client = new OkHttpClient().newBuilder()
                    .build();
        }

        public static OkSingleton getInstance()
        {
            return Holder.instance;
        }

        public OkHttpClient getClient()
        {
            return client;
        }
    }

    class ConnectionListener extends EventListener
    {
        @Override
        public void callFailed(Call call, @NonNull IOException ioe)
        {
            if (!call.isCanceled())
                onFailedConnection(msgConnLost);
        }
    }

    static class MyConnectionException extends IOException
    {
        MyConnectionException(String s)
        {
            super(s);
        }
    }
}





