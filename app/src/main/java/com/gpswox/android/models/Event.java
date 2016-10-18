package com.gpswox.android.models;

/**
 * Created by gintas on 30/11/15.
 */
public class Event
{
    public int id, user_id, device_id, position_id, alert_id;
    // geofence_id
    public String message;
    public String address;
    public float altitude;
    // course
    public float latitude, longitude;
    // power
    public float speed;
    public String time;
    // deleted

    public String device_name, geofence_name;

    public boolean fitForFilter(String filterString) {
        if(message.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(address.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(time.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(device_name.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(geofence_name.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        return false;
    }
}
