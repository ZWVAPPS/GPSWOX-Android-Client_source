package com.gpswox.android.models;

/**
 * Created by gintas on 12/12/15.
 */
public class HistorySensor
{
    public String id, name, sufix;

    public HistorySensor(String id, String name, String sufix)
    {
        this.id = id;
        this.name = name;
        this.sufix = sufix;
    }
    @Override
    public String toString() {
        return name;
    }
}
