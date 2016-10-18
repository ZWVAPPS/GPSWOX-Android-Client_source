package com.gpswox.android.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by gintas on 12/12/15.
 */
public class HistoryItem
{
    public int status;
    public String time;
    public String show, raw_time;
    public ArrayList<HistoryItemCoord> items;
    public Driver driver;

    public String getHint(ArrayList<HistoryItemClass> itemClasses) {
        String hint = "";
        for(HistoryItemClass itemClass : itemClasses)
            if(itemClass.id == status) {
                if (itemClass.value.equals("drive"))
                    hint = "Driving";
                else if (itemClass.value.equals("stop"))
                    hint = "Stopped";
                else if (itemClass.value.equals("start"))
                    hint = "Route begin";
                else if (itemClass.value.equals("end"))
                    hint = "Route end";
                else if (itemClass.value.equals("event"))
                    hint = "Event";
            }
        return hint;
    }

    public String getImageUrl(ArrayList<HistoryItemImage> images) {
        for (HistoryItemImage item : images)
            if(item.id == status)
                return item.value;
        return "";
    }

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public long getTimestamp()
    {
        try {
            return dateFormat.parse(raw_time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}