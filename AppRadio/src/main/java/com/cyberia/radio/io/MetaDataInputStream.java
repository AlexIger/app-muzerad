package com.cyberia.radio.io;

import android.text.TextUtils;
import com.cyberia.radio.eventbus.Events;
import com.cyberia.radio.helpers.ExceptionHandler;
import org.greenrobot.eventbus.EventBus;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class MetaDataInputStream extends FilterInputStream
{
    private static final String TAG = "MetaDataInputStream";
    /* meta data positions */
    int metaint;  //number of bytes between the metadata messages
    int next;  //number of bytes still to read up to the next metadata message

    private MetaDataInputStream(InputStream in)
    {
        super(in);
        metaint = ServerInfo.getIcyMetaInt();
        next = metaint;
    }

    public static MetaDataInputStream  getInstance(InputStream stream)
    {
        return new MetaDataInputStream(stream);
    }

    @Override
    public int read()
    {
        return 0;
    }

    @Override
    public int read(byte[] b)
    {
        return this.read(b, 0, b.length);
    }

    /**
     * The method splits the metadata from the mp3 stream.
     * The metadata info are sent every metaint bytes. The first
     * byte after metaint bytes of the stream indicates the length of
     * the metadata message.
     */

    @Override
    public int read(byte[] b, int off, int len)
    {

        int i = 0;

        try
        {
            //if the metadata interval is smaller the 100 bytes, metadata is not supported..
            if (metaint <= 100)
                return in.read(b, off, len);

            if (next < len)
            {
                //read up to the metadata position
                int part1 = in.read(b, off, next);

                if (part1 < 0)
                    return part1;

                //bytes to receive up to the next metadata position (if everything works correctly "next" should be 0)
                next -= part1;
                if (next > 0)
                    return part1;

                //extract the metadata from the stream
                processHeader();

                //read the rest of the stream into the buffer as requested
                //recursion, in case sb tries to read a block greater than meta-data interval
                int part2 = this.read(b, off + part1, len - part1);

                //in the case of an error / end => -1 return still the result of part1
                if (part2 < 0)
                    part2 = 0;
                return part1 + part2;
            }

            i = in.read(b, off, len);
            next -= i;

        }
        catch (Exception e)
        {
            ExceptionHandler.onException(TAG, e);
        }

        return i;
    }

    public String readHeader() throws Exception
    {
        next = metaint;
        byte[] b;


        while (in.available() < 1)
        {
            Thread.sleep(100);
        }

        int i = in.read();

        if (i <= 0) return null;

        i *= 16;
        b = new byte[i];

        //wait till enough bytes are available
        while (in.available() < i)
        {
            Thread.sleep(100);
        }

        in.read(b);

        return new String(b, StandardCharsets.UTF_8);
    }

    public void processHeader()
    {
        try
        {
            String metaString = readHeader();

            if (!TextUtils.isEmpty(metaString))
            {
                String[] split = metaString.split(";");

                int start = split[0].indexOf("'") + 1;
                int end = split[0].lastIndexOf("'");

                String result = metaString.substring(start, end);
                //replaceAll("\\.*\\]", "");
                fireMetadataEvent(result);
            }

        } catch (Exception e)
        {
            fireMetadataEvent("");
            ExceptionHandler.onException(TAG, e);
        }
    }

    public void fireMetadataEvent(String data)
    {
        EventBus.getDefault().post(new Events.SongMetadataEvent(data));
    }

    @Override
    public void close()
    {
        try
        {
            if (in != null)
                in.close();
        } catch (IOException e){}
    }
}


