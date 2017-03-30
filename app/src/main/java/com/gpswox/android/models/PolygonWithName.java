package com.gpswox.android.models;

import android.graphics.Paint;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

/**
 * Created by Mantoska on 27/03/2017.
 */

public class PolygonWithName
{
    private MarkerOptions markerOptions;
    private Marker marker;
    private Polygon polygon;
    private Paint paint;
    private Geofence geofence;

    public PolygonWithName(Polygon polygon, Paint paint, MarkerOptions markerOptions, Marker marker, Geofence geofence)
    {
        this.polygon = polygon;
        this.paint = paint;
        this.markerOptions = markerOptions;
        this.marker = marker;
        this.geofence = geofence;
    }

    public Polygon getPolygon()
    {
        return polygon;
    }

    public void setPolygon(Polygon polygon)
    {
        this.polygon = polygon;
    }

    public Paint getPaint()
    {
        return paint;
    }

    public void setPaint(Paint paint)
    {
        this.paint = paint;
    }

    public MarkerOptions getMarkerOptions()
    {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions)
    {
        this.markerOptions = markerOptions;
    }

    public Marker getMarker()
    {
        return marker;
    }

    public void setMarker(Marker marker)
    {
        this.marker = marker;
    }

    public Geofence getGeofence()
    {
        return geofence;
    }

    public void setGeofence(Geofence geofence)
    {
        this.geofence = geofence;
    }
}
