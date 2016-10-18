package com.gpswox.android.models;

public class FuelMeasurement
{
    public int id;
    public String title;
    public String fuel_title;
    public String distance_title;

    @Override
    public String toString() {
        return title;
    }
}