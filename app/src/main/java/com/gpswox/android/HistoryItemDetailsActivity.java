package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.models.HistoryItem;
import com.gpswox.android.models.HistoryItemClass;
import com.gpswox.android.models.HistoryItemCoord;
import com.gpswox.android.models.HistoryItemImage;
import com.gpswox.android.models.HistorySensorData;
import com.gpswox.android.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryItemDetailsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    @Bind(R.id.back)
    View back;
    @Bind(R.id.listview)
    ListView listview;
    @Bind(R.id.bottomButton)
    Button bottomButton;
    @Bind(R.id.map) View map_layout;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;
    GoogleMap map;
    HistoryItem item;
    ArrayList<HistoryItemClass> historyItemClasses;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item_coord_details);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listview.setVisibility(View.VISIBLE);
        map_layout.setVisibility(View.GONE);

        item = new Gson().fromJson(getIntent().getStringExtra("item"), HistoryItem.class);
        historyItemClasses = new Gson().fromJson(getIntent().getStringExtra("historyItemClasses"), new TypeToken<ArrayList<HistoryItemClass>>(){}.getType());
        ArrayList<HistoryItemImage> historyItemImages = new Gson().fromJson(getIntent().getStringExtra("historyItemImages"), new TypeToken<ArrayList<HistoryItemImage>>(){}.getType());

        ArrayList<Pair> array = new ArrayList<>();
        array.add(new Pair<>(getString(R.string.type)+":", item.getHint(historyItemClasses)));
        array.add(new Pair<>(getString(R.string.time)+":", item.raw_time));
        if(item.items.size() > 0) {
            array.add(new Pair<>(getString(R.string.latitude) + ":", item.items.get(0).lat));
            array.add(new Pair<>(getString(R.string.longitude) + ":", item.items.get(0).lng));
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(Double.valueOf(item.items.get(0).lat), Double.valueOf(item.items.get(0).lng), 1);
                if(addresses.size() > 0)
                {
                    array.add(new Pair<>(getString(R.string.address) + ":", addresses.get(0).getAddressLine(0)));
                }
            } catch (IOException e) { }
        }

        if(item.driver == null)
            array.add(new Pair<>(getString(R.string.driver) + ":", "-"));
        else
            array.add(new Pair<>(getString(R.string.driver) + ":", item.driver.name));

        float topSpeed = 0;
        for(HistoryItemCoord coord : item.items)
        {
            if (coord.sensors_data == null) {
                topSpeed = coord.speed;
            }
            else {
                for(HistorySensorData sensor : coord.sensors_data)
                    if(sensor.id.equals("speed"))
                        if(sensor.value > topSpeed)
                            topSpeed = sensor.value;
            }
        }
        array.add(new Pair<>(getString(R.string.topSpeed) + ":", topSpeed));

        float topAltitude = 0;
        for(HistoryItemCoord coord : item.items)
        {
            if (coord.sensors_data != null) {
                for(HistorySensorData sensor : coord.sensors_data)
                    if(sensor.id.equals("altitude"))
                        if(sensor.value > topAltitude)
                            topAltitude = sensor.value;
            }
        }
        array.add(new Pair<>(getString(R.string.topAltitude) + ":", topAltitude));

        listview.setAdapter(new AwesomeAdapter<Pair>(this, array)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_info_list, null);
                Pair item = getItem(position);
                TextView left = (TextView) convertView.findViewById(R.id.left);
                TextView right = (TextView) convertView.findViewById(R.id.right);

                left.setText(item.first != null ? String.valueOf(item.first) : "");
                right.setText(item.second != null ? String.valueOf(item.second) : "");
                return convertView;
            }
        });

        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listview.getVisibility() == View.VISIBLE)
                {
                    listview.setVisibility(View.GONE);
                    map_layout.setVisibility(View.VISIBLE);
                    bottomButton.setText(R.string.viewDetails);
                }
                else
                {
                    listview.setVisibility(View.VISIBLE);
                    map_layout.setVisibility(View.GONE);
                    bottomButton.setText(R.string.viewMap);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        setUpMap();
    }

    private void setUpMap()
    {
        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        if(item.items.size() > 0) {
            LatLng geopoint = new LatLng(Double.valueOf(item.items.get(0).lat), Double.valueOf(item.items.get(0).lng));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(geopoint, 14)));

            Drawable dr = getResources().getDrawable(R.drawable.ruler_marker);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();

            MarkerOptions m = new MarkerOptions();
            m.position(geopoint);
            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, Utils.dpToPx(this, 15), Utils.dpToPx(this, 15), true)));

            map.addMarker(m);


            if(item.getHint(historyItemClasses).equals("Driving")) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.parseColor("#819afc"));
                polylineOptions.width(Utils.dpToPx(HistoryItemDetailsActivity.this, 1));

                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (HistoryItemCoord coord : item.items)
                {
                    LatLng latlng = new LatLng(Double.valueOf(coord.lat), Double.valueOf(coord.lng));
                    polylineOptions.add(latlng);
                    builder.include(latlng);
                }
                map.addPolyline(polylineOptions);

                listview.setVisibility(View.GONE);
                map_layout.setVisibility(View.VISIBLE);
                bottomButton.setText(R.string.viewDetails);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Utils.dpToPx(HistoryItemDetailsActivity.this, 5)));
                    }
                }, 100);
            }
        }
    }
}
