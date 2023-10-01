package com.cyberia.radio.utils;

import android.text.TextUtils;

import com.cyberia.radio.helpers.ExceptionHandler;

public class StringCapitalizer
{

    public static String capitalize(String input)
    {
        if (input == null) input = "test";

        String[] split = input.split(",");

        String temp;
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < split.length; i++)
        {
            try
            {
                temp = split[i];
                char first = Character.toUpperCase(temp.charAt(0));

                if(i == (split.length-1))
                {
                    buff.append(first).append(temp.substring(1));
                }
                else
                {
                    buff.append(first).append(temp.substring(1)).append(" ").append('\u2022').append(" ");
                }
            } catch (Exception e)
            {
               ExceptionHandler.onException("String Capitalizer", e);
            }

//            result += Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
        }
        return  buff.toString();
    }

    public static String capitalizeBlanks(String input)
    {
        if (TextUtils.isEmpty(input))
           return "";

        String[] split = input.split(",");

        String temp;
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < split.length; i++)
        {
            try
            {
                temp = split[i];
                char first = Character.toUpperCase(temp.charAt(0));

                if(i == (split.length-1))
                {
                    buff.append(first).append(temp.substring(1));
                }
                else
                {
//                    buff.append(first + temp.substring(1) + " " + '\u2022' + " ");

                    buff.append(first).append(temp.substring(1)).append(" ").append(" ").append(" ");
                }
            } catch (Exception e)
            {
                ExceptionHandler.onException("String Capitalizer", e);
            }

//            result += Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
        }
        return  buff.toString();
    }
}
