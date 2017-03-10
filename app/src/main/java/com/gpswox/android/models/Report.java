package com.gpswox.android.models;

import java.util.ArrayList;

/**
 * Created by gintas on 12/11/15.
 */
public class Report
{
    public int id, user_id, type, show_addresses, stops;
    public String title, email;
    public String format, dateFrom, dateTo;
    public int daily, weekly, zones_instead;
    public int speed_limit;
    public ArrayList<Integer> devices;
    public ArrayList<Integer> geofences;

    public Report()
    {
        title = "";
        email = "";
        format = "";
        devices = new ArrayList<>();
        geofences = new ArrayList<>();
    }
 /*           "zones_instead": null,
            "weekly_email_sent": "2015-11-09 00:00:00",
            "daily_email_sent": "2015-11-12 00:00:00"*/
}
