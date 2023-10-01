package com.cyberia.radio.history;

import com.cyberia.radio.model.StationCookie;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragmentModel
{
    static List<StationCookie> getAllHistory()
    {

        return new ArrayList<>(HistoryManager.loadHistory());
    }

    static void updateHistoryList(int position)
    {
        HistoryManager.updateHistory(position);
    }


}