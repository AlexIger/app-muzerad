package com.cyberia.radio.eventbus;

public abstract class Events
{
    static public class FailEvent
    {
        public final String message;

        public FailEvent(String message)
        {
            this.message = message;
        }
    }

    static public class SongMetadataEvent
    {
        public String metadata;

        public SongMetadataEvent(String meta)
        {
            metadata = meta;
        }
    }

    static public class BluetoothEvent
    {
        public boolean isDisconnected;

        public BluetoothEvent(boolean isConnected)
        {
            this.isDisconnected = isConnected;
        }
    }

    static public class BufferingEvent
    {
        public final int increment;

        public BufferingEvent(int increment)
        {
            this.increment = increment;
        }
    }

    static public class ConnectionStatusEvent
    {
        public final String status;

        public ConnectionStatusEvent(String status)
        {
            this.status = status;
        }
    }

}
