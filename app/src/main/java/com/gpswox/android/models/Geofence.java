package com.gpswox.android.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by gintas on 05/12/15.
 */
public class Geofence
{
    public int id, user_id, active;
    public String name;
    public String coordinates;
    public String polygon_color;
    public List<LatLng> coordinatesList;
}
