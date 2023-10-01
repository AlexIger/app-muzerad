package com.cyberia.radio;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cyberia.radio.helpers.MyPrint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class EspressoTest
{
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void view_isCorrect()
    {
        onView(withId(R.id.sliding_layout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void searhView_isCorrect()
    {
        onView(withId(R.id.bottom_panel))
                .check(matches(isDisplayed()));
    }

    @Test
    public void useAppContext()
    {
// Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        MyPrint.printOut("Espresso Test: ", appContext.getPackageName());

        assertEquals("com.cyberia.radio.AppRadio",
                appContext.getPackageName());
    }
}


