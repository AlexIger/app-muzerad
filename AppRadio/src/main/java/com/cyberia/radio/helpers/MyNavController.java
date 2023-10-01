package com.cyberia.radio.helpers;

import com.cyberia.radio.R;
import com.cyberia.radio.global.MyAppContext;
import com.cyberia.radio.interfaces.Controller;

public class MyNavController
{
    private static String category;

    public static String getCategory()
    {
        return category;
    }

    public static void setCategory(String _category)
    {
        category = _category;
    }

    public static void updateAppBar(Controller controller, final String prevTitle, final String prevSubtitle)
    {
        boolean isBackButton;

        isBackButton = !MyAppContext.INSTANCE.getAppContext().getString(R.string.appbar_home).equalsIgnoreCase(prevTitle);

        //update previous screen appbar
        controller.updateAppBarTitle(prevTitle, prevSubtitle, isBackButton);

    }
}
