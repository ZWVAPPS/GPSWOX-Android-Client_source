package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.DeviceIcon;
import com.gpswox.android.models.Sensor;
import com.gpswox.android.models.TailItem;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final String TAG = "MapActivity";
    @Bind(R.id.back) View back;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;
    @Bind(R.id.updatetimer) TextView updatetimer;
    @Bind(R.id.autozoom) ImageView autozoom;
    @Bind(R.id.showtails) ImageView showtails;
    //@Bind(R.id.map) MapView map;
    private GoogleMap map;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;
    private Timer timer;
    private int autoZoomedTimes = 0;// dėl bugo osmdroid library, zoom'inam du kartus ir paskui po refresh'o nebe, nes greičiausiai user'is bus pakeitęs zoom'ą

    private HashMap<Integer, Marker> deviceIdMarkers;
    private HashMap<String, Device> markerIdDevices;
    private HashMap<Integer, Polyline> deviceIdPolyline;
    private HashMap<Integer, LatLng> deviceIdLastLatLng;
//    private HashMap<Integer, Marker> deviceIdSmallMarkerInfo;
    private long lastRefreshTime;
    boolean isAutoZoomEnabled;
    boolean isShowTitlesEnabled;
    boolean isShowTailsEnabled = true;
    private String stopTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        deviceIdMarkers = new HashMap<>();
        markerIdDevices = new HashMap<>();
        deviceIdPolyline = new HashMap<>();
        deviceIdLastLatLng = new HashMap<>();
//        deviceIdSmallMarkerInfo = new HashMap<>();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        loading_layout.setVisibility(View.VISIBLE);
        content_layout.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        refresh();

        autozoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAutoZoomEnabled = !isAutoZoomEnabled;
                if(isAutoZoomEnabled) {
                    autozoom.setImageResource(R.drawable.autozoom_enabled);
                    Toast.makeText(MapActivity.this, R.string.autozoom_enabled, Toast.LENGTH_SHORT).show();
                }
                else {
                    autozoom.setImageResource(R.drawable.autozoom_disabled);
                    Toast.makeText(MapActivity.this, R.string.autozoom_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });

        showtails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowTailsEnabled = !isShowTailsEnabled;
                if(isShowTailsEnabled) {
                    showtails.setImageResource(R.drawable.tail_active);
                    for(Polyline polyline : deviceIdPolyline.values())
                        polyline.setVisible(true);
                }
                else {
                    showtails.setImageResource(R.drawable.tail_inactive);
                    for(Polyline polyline : deviceIdPolyline.values())
                        polyline.setVisible(false);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float timeleft = 10 - Math.round(System.currentTimeMillis() - lastRefreshTime) / 1000f;
                        if(timeleft < 0)
                            timeleft = 0;
                        updatetimer.setText(String.format("%.0f", timeleft));
                        if(System.currentTimeMillis() - lastRefreshTime >= 10 * 1000)
                            refresh();
                    }
                });
            }
        }, 0, 1 * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            timer.cancel();
            timer.purge();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void refresh()
    {
        lastRefreshTime = System.currentTimeMillis();
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, Lang.getCurrentLanguage(), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
            @Override
            public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                Log.d(TAG, "success: loaded devices array");
                final ArrayList<Device> allDevices = new ArrayList<>();
                if(getDevicesItems != null)
                    for(ApiInterface.GetDevicesItem item : getDevicesItems)
                        allDevices.addAll(item.items);
                API.getApiInterface(MapActivity.this).getFieldsDataForEditing(api_key, Lang.getCurrentLanguage(), 1, new Callback<ApiInterface.GetFieldsDataForEditingResult>() {
                    @Override
                    public void success(final ApiInterface.GetFieldsDataForEditingResult getFieldsDataForEditingResult, Response response)
                    {
                        Log.d(TAG, "success: loaded icons");
                        new AsyncTask<Void, Void, Void>() {
                            ArrayList<MarkerOptions> markers;
                            ArrayList<Integer> deviceIds;
                            @Override
                            protected Void doInBackground(Void... params) {
                                // add markers
                                int dp100 = Utils.dpToPx(MapActivity.this, 50);
                                markers = new ArrayList<>();
                                deviceIds = new ArrayList<>();
                                if(getFieldsDataForEditingResult == null || getFieldsDataForEditingResult.device_icons == null) return null;
                                for(Device item : allDevices)
                                {
                                    if(item.device_data.active == 1)
                                    {
                                        // ieškom ikonos masyve
                                        DeviceIcon mapIcon = null;
                                        for(DeviceIcon icon : getFieldsDataForEditingResult.device_icons)
                                            if(item.device_data.icon_id == icon.id)
                                                mapIcon = icon;

                                        String server_base = (String) DataSaver.getInstance(MapActivity.this).load("server_base");

                                        try {
                                            Log.d("MapActivity", "DOWNLOADING BITMAP: " + server_base + mapIcon.path);
                                            Bitmap bmp = BitmapFactory.decodeStream(new URL(server_base + mapIcon.path).openConnection().getInputStream());

                                            int srcWidth = bmp.getWidth();
                                            int srcHeight = bmp.getHeight();

                                            int maxWidth = Utils.dpToPx(MapActivity.this, mapIcon.width);
                                            int maxHeight = Utils.dpToPx(MapActivity.this, mapIcon.height);

                                            float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                                            int dstWidth = (int) (srcWidth * ratio);
                                            int dstHeight = (int) (srcHeight * ratio);

                                            bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);

                                            // marker

                                            MarkerOptions m = new MarkerOptions();
                                            m.position(new LatLng(item.lat, item.lng));
//                                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                            marker.setIcon(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                                            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));

                                            // info window
//                                            MapMarkerInfoWindow infoWindow = new MapMarkerInfoWindow(MapActivity.this, item, R.layout.layout_map_infowindow, map);
//                                            marker.setInfoWindow(infoWindow);

                                            markers.add(m);
                                            deviceIds.add(item.id);
                                        } catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                ArrayList<GeoPoint> points = new ArrayList<>();

                                if(autoZoomedTimes < 1)
                                {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(markers.size() > 1) {
                                                        try {
                                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                            for (MarkerOptions item : markers)
                                                                builder.include(item.getPosition());
                                                            LatLngBounds bounds = builder.build();
//                                int padding = 0; // offset from edges of the map in pixels
                                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPx(MapActivity.this, 50));
                                                            map.animateCamera(cu);
                                                        } catch (Exception e) {

                                                        }
                                                    }
                                                    else if(markers.size() > 0)
                                                    {
                                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15));
                                                    }
                                                    autoZoomedTimes++;
                                                }
                                            });
                                        }
                                    }, 50);
                                }
                                else if(isAutoZoomEnabled) {
                                    if(markers.size() > 1) {
                                        try {
                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            for (MarkerOptions item : markers)
                                                builder.include(item.getPosition());
                                            LatLngBounds bounds = builder.build();
//                                int padding = 0; // offset from edges of the map in pixels
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPx(MapActivity.this, 50));
                                            map.animateCamera(cu);
                                        } catch (Exception e) {

                                        }
                                    }
                                    else if(markers.size() > 0)
                                    {
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15));
                                    }
                                    autoZoomedTimes++;
                                }

                                Log.d(TAG, "onPostExecute: icons downloaded and added to map, total markers: " + markers.size());

                                loading_layout.setVisibility(View.GONE);
                                if(markers.size() != 0)
                                    content_layout.setVisibility(View.VISIBLE);
                                else
                                    nodata_layout.setVisibility(View.VISIBLE);

                                for(int i = 0; i < markers.size(); i++)
                                {
                                    MarkerOptions options = markers.get(i);
                                    int deviceId = deviceIds.get(i);

                                    Marker m;

                                    Polyline polyline;
                                    if(deviceIdMarkers.containsKey(deviceId)) {
                                        Log.d("aa", "moving to" + options.getPosition());
                                        deviceIdMarkers.get(deviceId).setPosition(new LatLng(options.getPosition().latitude, options.getPosition().longitude));
                                        m = deviceIdMarkers.get(deviceId);

                                        polyline = deviceIdPolyline.get(deviceId);
                                    }
                                    else
                                    {
                                        Log.d("aa", "putting new");
                                        m = map.addMarker(options);
                                        deviceIdMarkers.put(deviceId, m);
                                        polyline = map.addPolyline(new PolylineOptions());
                                        deviceIdPolyline.put(deviceId, polyline);
                                    }

                                    Device thatonedevice = null;
                                    for(Device device : allDevices)
                                        if(device.id == deviceId)
                                            thatonedevice = device;
                                    markerIdDevices.put(m.getId(), thatonedevice);


                                    // update marker rotation based on driving direction
                                    if(thatonedevice != null && deviceIdLastLatLng.containsKey(deviceId))
                                    {
                                        double dirLat = thatonedevice.lat - deviceIdLastLatLng.get(deviceId).latitude;
                                        double dirLng = thatonedevice.lng - deviceIdLastLatLng.get(deviceId).longitude;

                                        m.setRotation((float) Math.toDegrees(Math.atan2(dirLng, dirLat)));
                                    }
                                    deviceIdLastLatLng.put(deviceId, new LatLng(thatonedevice.lat, thatonedevice.lng));

                                    List<LatLng> polylinePoints = new ArrayList<>();
                                    for(TailItem item : thatonedevice.tail)
                                        polylinePoints.add(new LatLng(Double.valueOf(item.lat), Double.valueOf(item.lng)));
                                    polyline.setPoints(polylinePoints);
                                    polyline.setWidth(Utils.dpToPx(MapActivity.this, 2));
                                    polyline.setColor(Color.parseColor(thatonedevice.device_data.tail_color));
                                }


                                // else

                                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                    @Override
                                    public View getInfoWindow(Marker marker) {
                                        return null;
                                    }

                                    @Override
                                    public View getInfoContents(final Marker marker)
                                    {
                                        synchronized (this) {

                                        }
                                        final Device device = markerIdDevices.get(marker.getId());

                                        View view = getLayoutInflater().inflate(R.layout.layout_map_infowindow, null);
                                        view.bringToFront();
                                        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                marker.hideInfoWindow();
                                            }
                                        });
                                        TextView device_name = (TextView) view.findViewById(R.id.device_name);
                                        device_name.setText(device.name);
                                        TextView altitude = (TextView) view.findViewById(R.id.altitude);
                                        altitude.setText(String.valueOf(device.altitude) + " " + device.unit_of_altitude);
                                        TextView time = (TextView) view.findViewById(R.id.time);
                                        time.setText(device.time);
                                        TextView stopTimev = (TextView) view.findViewById(R.id.stopTime);
                                        stopTimev.setText(stopTime);
                                        TextView speed = (TextView) view.findViewById(R.id.speed);
                                        speed.setText(device.speed + " " + device.distance_unit_hour);
                                        TextView address = (TextView) view.findViewById(R.id.address);
                                        address.setText(device.address);

                                        final ArrayList<Sensor> showableSensors = new ArrayList<>();
                                        for(Sensor item : device.sensors)
                                            if(item.show_in_popup > 0)
                                                showableSensors.add(item);

                                        ListView sensors_list = (ListView) view.findViewById(R.id.sensors_list);
                                        sensors_list.setAdapter(new AwesomeAdapter<Sensor>(MapActivity.this)
                                        {
                                            @Override
                                            public int getCount() {
                                                return showableSensors.size();
                                            }

                                            @Override
                                            public View getView(int position, View convertView, ViewGroup parent) {
                                                if(convertView == null)
                                                    convertView = getLayoutInflater().inflate(R.layout.adapter_map_sensorslist, null);

                                                Sensor item = showableSensors.get(position);
                                                TextView name = (TextView) convertView.findViewById(R.id.name);
                                                name.setText(item.name);
                                                TextView value = (TextView) convertView.findViewById(R.id.value);
                                                value.setText(item.value);
                                                return convertView;
                                            }
                                        });

                                        List<Address> addresses;
                                        try {
                                            addresses = new Geocoder(MapActivity.this).getFromLocation(device.lat, device.lng, 1);
                                            if (addresses.size() > 0)
                                                address.setText(addresses.get(0).getAddressLine(0));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        return view;
                                    }
                                });
                                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(final Marker marker) {
                                        stopTime = "...";
                                        final Device device = markerIdDevices.get(marker.getId());
                                        API.getApiInterface(MapActivity.this).deviceStopTime((String) DataSaver.getInstance(MapActivity.this).load("api_key"), "en", device.id, new Callback<ApiInterface.DeviceStopTimeResult>()
                                        {
                                            @Override
                                            public void success(ApiInterface.DeviceStopTimeResult result, Response response)
                                            {
                                                stopTime = result.time;
                                                marker.showInfoWindow();
                                            }

                                            @Override
                                            public void failure(RetrofitError retrofitError) {}
                                        });

                                        return false;
                                    }
                                });
                                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) {
                                        marker.hideInfoWindow();
                                    }
                                });

//                                updateSmallMarkerData(allDevices);
                            }
                        }.execute();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(MapActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MapActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
    }

    /*private void updateSmallMarkerData(ArrayList<Device> allDevices)
    {
        for(Device device : allDevices)
        {
            Marker marker = deviceIdSmallMarkerInfo.get(device.id);
            if(marker == null)
            {
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(device.lat, device.lng));
                options.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                marker = map.addMarker(options);
            }

        }
    }

    private boolean devicesArrayContainsId(ArrayList<Device> array, int id)
    {
        for(Device item : array)
            if(item.id == id)
                return true;
        return false;
    }*/
}
