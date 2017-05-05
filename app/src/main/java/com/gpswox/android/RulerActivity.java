package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RulerActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final String TAG = "RulerActivity";
    @Bind(R.id.back)
    View back;
    @Bind(R.id.distance)
    TextView distance;

    @Bind(R.id.zoom_in)
    View zoom_in;
    @Bind(R.id.zoom_out)
    View zoom_out;
    @Bind(R.id.loading_layout)
    View loading_layout;
    @Bind(R.id.mapLayout)
    View mapLayout;
    //@Bind(R.id.content_layout) View content_layout;

    private GoogleMap map;
    private PolylineOptions polylineOptions = new PolylineOptions();
    private Polyline polyline;
    private boolean clickedOnFirstMarker = false;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        zoom_in.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        loading_layout.setVisibility(View.GONE);
        mapLayout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(RulerActivity.this).getDevices(api_key, Lang.getCurrentLanguage(), new Callback<ArrayList<ApiInterface.GetDevicesItem>>()
        {
            @Override
            public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                final ArrayList<Device> allDevices = new ArrayList<>();
                if (getDevicesItems != null)
                    for (ApiInterface.GetDevicesItem item : getDevicesItems)
                        allDevices.addAll(item.items);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Device item : allDevices)
                    if (item.device_data.active == 1)
                        builder.include(new LatLng(item.lat, item.lng));


                LatLngBounds bounds = builder.build();

                CameraUpdate center =
                        CameraUpdateFactory.newLatLngBounds(bounds, 10);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

                map.moveCamera(center);
                map.animateCamera(zoom);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(RulerActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static float distBetween(double lat1, double lng1, double lat2, double lng2)
    {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return (float) (dist * meterConversion);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {

            @Override
            public void onMapClick(LatLng point)
            {
                polylineOptions.add(point);
                if (clickedOnFirstMarker)
                {
                    polyline.remove();
                    marker.remove();

                    float totalDistance = 0;
                    for (int i = 1; i < polylineOptions.getPoints().size(); i++)
                    {

                        float distance = distBetween(polylineOptions.getPoints().get(i - 1).latitude, polylineOptions.getPoints().get(i - 1).longitude, polylineOptions.getPoints().get(i).latitude, polylineOptions.getPoints().get(i).longitude);
                        ;
                        totalDistance += distance / 1000;
                    }
                    distance.setText(String.format(getString(R.string.rulerDistance), totalDistance));
                }
                clickedOnFirstMarker = true;

                polyline = map.addPolyline(polylineOptions.width(5)
                        .color(Color.RED));
                // Marker
                Drawable dr = getResources().getDrawable(R.drawable.ruler_marker);
                Bitmap bmp = ((BitmapDrawable) dr).getBitmap();

                int srcWidth = bmp.getWidth();
                int srcHeight = bmp.getHeight();

                int maxWidth = Utils.dpToPx(RulerActivity.this, 10);
                int maxHeight = Utils.dpToPx(RulerActivity.this, 10);
                int dp100 = Utils.dpToPx(RulerActivity.this, 50);

                float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                int dstWidth = (int) (srcWidth * ratio);
                int dstHeight = (int) (srcHeight * ratio);

                bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);

                MarkerOptions mo = new MarkerOptions();
                mo.position(point);
                mo.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                marker = map.addMarker(mo);
            }
        });
    }
}
