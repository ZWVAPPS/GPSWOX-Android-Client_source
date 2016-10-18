package com.gpswox.android.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.gpswox.android.adapters.AwesomeAdapter;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Utils
{
	public static int dpToPx(Context context, int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void setGroupClickListenerToNotify(final ExpandableListView view, final AwesomeAdapter adapter)
    {
        view.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    public static void setGroupClickListenerToNotify(final ExpandableListView view, final BaseExpandableListAdapter adapter)
    {
        view.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    public static GeoPoint getCentralGeoPoint(List<GeoPoint> geoCoordinates)
    {
        if (geoCoordinates.size() == 1)
        {
            return geoCoordinates.get(0);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (GeoPoint geoCoordinate : geoCoordinates)
        {
            double latitude = geoCoordinate.getLatitude() * Math.PI / 180;
            double longitude = geoCoordinate.getLongitude() * Math.PI / 180;

            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }

        int total = geoCoordinates.size();

        x = x / total;
        y = y / total;
        z = z / total;

        double centralLongitude = Math.atan2(y, x);
        double centralSquareRoot = Math.sqrt(x * x + y * y);
        double centralLatitude = Math.atan2(z, centralSquareRoot);

        return new GeoPoint(centralLatitude * 180 / Math.PI, centralLongitude * 180 / Math.PI);
    }

    public static <T> ArrayList<T> reverseArrayList(ArrayList<T> list) {
        int length = list.size();
        ArrayList<T> result = new ArrayList<T>(length);

        for (int i = length - 1; i >= 0; i--) {
            result.add(list.get(i));
        }

        return result;
    }

    public static String getStrInBetween(String str, String left, String right)
    {
        return str.substring(str.indexOf(left) + 1, str.indexOf(right));
    }
}
