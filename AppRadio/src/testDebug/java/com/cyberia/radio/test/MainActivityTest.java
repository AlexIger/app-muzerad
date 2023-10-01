package com.cyberia.radio.test;

import com.cyberia.radio.helpers.MyPrint;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class MainActivityTest
{
    @Test
    @DisplayName("My Test")
    public void onStationInfoAvailable()
    {
        Assert.assertEquals(4+4, 8);
//        Assert.assertNull(null);
    }
}