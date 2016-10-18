package com.gpswox.android.models;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by gintas on 20/12/15.
 */
public class PrecalculatedGraphData
{
    public String sensor_id;
    public ArrayList<Float> sensorDataValues = new ArrayList<>();
    public ArrayList<Long> sensorDataTimestamps = new ArrayList<>();
    public ArrayList<Entry> yVals;
    public ArrayList<String> xVals;
}
