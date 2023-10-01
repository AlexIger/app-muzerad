package com.cyberia.radio.io;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.io.InputStream;


public class MyInputStreamDataSource implements DataSource
{
    private final DataSpec dataSpec;
    private final InputStream inputStream;


    public MyInputStreamDataSource(DataSpec dataSpec, InputStream inputStream)
    {
        this.dataSpec = dataSpec;
        this.inputStream = inputStream;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws IOException
    {
        return inputStream.available();
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException
    {
        int bytesRead = inputStream.read(buffer, offset, readLength);

        if (bytesRead == C.RESULT_END_OF_INPUT) {
            return -1;
        } else {
            return bytesRead;
        }
    }

    @Override
    public Uri getUri()
    {
        return dataSpec.uri;
    }


    @Override
    public void close()
    {
        try
        {
            if (inputStream != null)
                inputStream.close();
        } catch (IOException e)
        {
        }
    }

    @Override
    public void addTransferListener(@NonNull TransferListener transferListener)
    {
    }
}