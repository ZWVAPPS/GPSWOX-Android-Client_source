package com.gpswox.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddGeofenceActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final String TAG = "AddGeofenceActivity";
    private Integer activeDevices = 0;
    @Bind(R.id.back) View back;
    @Bind(R.id.addGeofence) View addGeofence;
    @Bind(R.id.name) EditText name;
    @Bind(R.id.bgcolor) EditText bgcolor;
    @Bind(R.id.drawPolygon) View drawPolygon;
    @Bind(R.id.setData) View setData;

    @Bind(R.id.dataLayout) View dataLayout;
    @Bind(R.id.mapLayout) View mapLayout;

    private GoogleMap map;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;

    @Bind(R.id.loading_layout) View loading_layout;

    private PolygonOptions polyOptions = new PolygonOptions();
    private Polygon polygon;
    private boolean clickedOnFirstMarker = false;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);
        ButterKnife.bind(this);

        final Activity activity = AddGeofenceActivity.this;

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        drawPolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.VISIBLE);
                dataLayout.setVisibility(View.GONE);
            }
        });

        setData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataLayout.setVisibility(View.VISIBLE);
                mapLayout.setVisibility(View.GONE);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                polyOptions.add(point);
                if (clickedOnFirstMarker) {
                    polygon.remove();
                    marker.remove();
                }
                clickedOnFirstMarker = true;
                String color = bgcolor.getText().toString();
                int r = Integer.valueOf( color.substring( 1, 3 ), 16 );
                int g = Integer.valueOf( color.substring( 3, 5 ), 16 );
                int b = Integer.valueOf( color.substring( 5, 7 ), 16 );
                polygon = map.addPolygon(polyOptions.strokeColor(Color.argb(200, r, g, b)).fillColor(Color.argb(100, r, g, b)));
                // Marker
                Drawable dr = getResources().getDrawable(R.drawable.ruler_marker);
                Bitmap bmp = ((BitmapDrawable) dr).getBitmap();

                int srcWidth = bmp.getWidth();
                int srcHeight = bmp.getHeight();

                int maxWidth = Utils.dpToPx(AddGeofenceActivity.this, 10);
                int maxHeight = Utils.dpToPx(AddGeofenceActivity.this, 10);
                int dp100 = Utils.dpToPx(AddGeofenceActivity.this, 50);

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

        addGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<LatLng> points = polygon != null ? polygon.getPoints() : null;
                if (points == null)
                    Toast.makeText(AddGeofenceActivity.this, R.string.noPolygonData, Toast.LENGTH_SHORT).show();
                if (points.size() < 4)
                    Toast.makeText(AddGeofenceActivity.this, R.string.polygonIsntClosed, Toast.LENGTH_SHORT).show();
                else if (name.getText().toString().equals(""))
                    Toast.makeText(AddGeofenceActivity.this, R.string.nameMustBeSet, Toast.LENGTH_SHORT).show();
                else if (bgcolor.getText().toString().equals(""))
                    Toast.makeText(AddGeofenceActivity.this, R.string.bgcolorMustBeSet, Toast.LENGTH_SHORT).show();
                else {
                    try {
                        String nameStr = name.getText().toString();
                        String bgcolorStr = bgcolor.getText().toString();
                        JSONArray arr = new JSONArray();
                        for (LatLng point : points) {
                            JSONObject obj = new JSONObject();
                            obj.put("lat", point.latitude);
                            obj.put("lng", point.longitude);
                            arr.put(obj);
                        }
                        String polygon_array = arr.toString();
                        API.getApiInterface(AddGeofenceActivity.this).addNewGeofence((String) DataSaver.getInstance(AddGeofenceActivity.this).load("api_key"), Lang.getCurrentLanguage(), nameStr, bgcolorStr, polygon_array, new Callback<ApiInterface.AddNewGeofenceResult>() {
                            @Override
                            public void success(ApiInterface.AddNewGeofenceResult addNewGeofenceResult, Response response) {
                                Toast.makeText(AddGeofenceActivity.this, R.string.geofenceAdded, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Toast.makeText(AddGeofenceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddGeofenceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        bgcolor.setText("#dfa7b5");

        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getGeofenceData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetGeofenceDataResult>() {
            @Override
            public void success(ApiInterface.GetGeofenceDataResult getGeofenceDataResult, Response response) {
                API.getApiInterface(AddGeofenceActivity.this).getDevices(api_key, Lang.getCurrentLanguage(), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
                    @Override
                    public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response) {
                        Log.d(TAG, "success: loaded devices array");
                        final ArrayList<Device> allDevices = new ArrayList<>();
                        if (getDevicesItems != null)
                            for (ApiInterface.GetDevicesItem item : getDevicesItems)
                                allDevices.addAll(item.items);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for(Device item : allDevices) {
                            if(item.device_data.active == 1) {
                                activeDevices++;
                                builder.include(new LatLng(item.lat, item.lng));
                            }
                        }

                        final LatLngBounds bounds = builder.build();
                        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                if (activeDevices > 1) {
                                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPx(AddGeofenceActivity.this, 50)));
                                }
                                else {
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
                                }
                                map.setOnCameraChangeListener(null);
                            }
                        });

                        loading_layout.setVisibility(View.GONE);
                        dataLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(AddGeofenceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() == 403) {
                    Toast.makeText(AddGeofenceActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(AddGeofenceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
    }
}
