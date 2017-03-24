package com.gpswox.android.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by gintas on 12/12/15.
 */
public class HistoryItemCoord
{
    public String other;
    public float speed;
    public float altitude;
    public ArrayList<HistorySensorData> sensors_data;
    public String raw_time;
    public String lat, lng;

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
