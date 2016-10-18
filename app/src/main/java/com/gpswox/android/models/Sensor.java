package com.gpswox.android.models;

/**
 * Created by gintas on 10/10/15.
 */
public class Sensor
{
    public int id;
    public String type;
    public String type_title;
    public String name;
    public String value;
    public int show_in_popup;

    public Sensor(Sensor another)
    {
        this.id = another.id;
        this.type = another.type;
        this.type_title = another.type_title;
        this.name = another.name;
        this.value = another.value;
    }
}