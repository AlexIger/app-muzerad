package com.cyberia.radio.global;

import com.cyberia.radio.helpers.MyPrint;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum MyThreadPool
{
    INSTANCE;

    private ExecutorService executorService;

    MyThreadPool()
    {
    }

    public ExecutorService getExecutorService()
    {
        synchronized (this)
        {
            if (executorService == null || executorService.isShutdown())
            {
                executorService = new ThreadPoolExecutor(
                        3, Integer.MAX_VALUE, 100, TimeUnit.SECONDS, new SynchronousQueue<>());

                MyPrint.printOut("Thread Pool", "Pool initialized");
            }
            return executorService;
        }
    }

}
