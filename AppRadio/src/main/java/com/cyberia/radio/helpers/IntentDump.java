package com.cyberia.radio.helpers;

import android.content.Intent;
import android.os.Bundle;
import java.util.Iterator;
import java.util.Set;

public class IntentDump
{
    public static void dumpIntent(Intent i)
    {
        if(Debug.ENABLED)
        {
            Bundle bundle = i.getExtras();
            if (bundle != null) {
                Set<String> keys = bundle.keySet();
                Iterator<String> it = keys.iterator();
                MyPrint.printOut("IntentDump","Dumping Intent start");
                while (it.hasNext()) {
                    String key = it.next();
                    MyPrint.printOut("IntentDump", "[" + key + "=" + bundle.get(key)+"]");
                }
                MyPrint.printOut("IntentDump","Dumping Intent end");
            }
        }
    }
}
