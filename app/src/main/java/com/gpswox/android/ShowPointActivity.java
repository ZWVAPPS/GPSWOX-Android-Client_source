package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShowPointActivity extends AppCompatActivity implements OnMapReadyCallback
{
    @Bind(R.id.back)
    View back;
    @Bind(R.id.latitude)
    EditText latitude;
    @Bind(R.id.longitude)
    EditText longitude;
    @Bind(R.id.showPoint)
    View showPoint;
    @Bind(R.id.mapLayout)
    View mapLayout;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_point);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        showPoint.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(StringUtils.isEmpty(latitude.getText()) || StringUtils.isEmpty(longitude.getText()))
                {
                    Toast.makeText(ShowPointActivity.this, R.string.allFieldsMustBeFilled, Toast.LENGTH_SHORT).show();
                    return;
                }
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(ShowPointActivity.this);
                mapLayout.setVisibility(View.VISIBLE);
            }

        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

        final double lat = Double.parseDouble(latitude.getText().toString());
        final double lng = Double.parseDouble(longitude.getText().toString());

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 9));

        map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
    }
}
