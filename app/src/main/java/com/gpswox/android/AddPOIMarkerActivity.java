package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.MapIcon;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddPOIMarkerActivity extends AppCompatActivity implements MapEventsReceiver {

    @Bind(R.id.back) View back;
    @Bind(R.id.addMarker) View addMarker;
    @Bind(R.id.icons_list) GridView icons_list;
    @Bind(R.id.name) EditText name;
    @Bind(R.id.description) EditText description;
    @Bind(R.id.setLocation) View setLocation;
    @Bind(R.id.setData) View setData;

    @Bind(R.id.dataLayout) View dataLayout;
    @Bind(R.id.mapLayout) View mapLayout;

    @Bind(R.id.map) MapView map;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;

    @Bind(R.id.loading_layout) View loading_layout;

    private MapIcon selectedIcon;
    private ItemizedIconOverlay<OverlayItem> overlay;
    private OverlayItem olItem;
    private GeoPoint currentPos;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poimarker);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final AwesomeAdapter<MapIcon> iconsAdapter = new AwesomeAdapter<MapIcon>(this) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_editobject_icons, null);

                MapIcon item = getItem(position);
                ImageView image = (ImageView) convertView.findViewById(R.id.imageview);
                String server = (String) DataSaver.getInstance(AddPOIMarkerActivity.this).load("server_base");
                Picasso.with(AddPOIMarkerActivity.this).load(server + item.path).into(image);

                if(selectedIcon.id == item.id)
                    convertView.setBackgroundResource(R.drawable.selected_icon_border);
                else
                    convertView.setBackgroundColor(0xFFFFFFFF);
                return convertView;
            }
        };
        icons_list.setAdapter(iconsAdapter);


        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getPOIMapIcons(api_key, getResources().getString(R.string.lang), new Callback<ApiInterface.GetPOIMapIconsResult>() {
            @Override
            public void success(ApiInterface.GetPOIMapIconsResult getPOIMapIconsResult, Response response) {
                selectedIcon = getPOIMapIconsResult.items.get(0);
                iconsAdapter.setArray(getPOIMapIconsResult.items);

                API.getApiInterface(AddPOIMarkerActivity.this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
                    @Override
                    public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response) {
                        final ArrayList<Device> allDevices = new ArrayList<>();
                        if (getDevicesItems != null)
                            for (ApiInterface.GetDevicesItem item : getDevicesItems)
                                allDevices.addAll(item.items);

                        ArrayList<GeoPoint> points = new ArrayList<>();
                        for(Device item : allDevices)
                            if(item.device_data.active == 1)
                                points.add(new GeoPoint(item.lat, item.lng));

                        map.getController().setZoom(14);
                        map.getController().animateTo(Utils.getCentralGeoPoint(points));

                        loading_layout.setVisibility(View.GONE);
                        dataLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (retrofitError.getResponse().getStatus() == 403) {
                            Toast.makeText(AddPOIMarkerActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AddPOIMarkerActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                        onBackPressed();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() == 403) {
                    Toast.makeText(AddPOIMarkerActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(AddPOIMarkerActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
            }
        });
        icons_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIcon = iconsAdapter.getItem(position);
                iconsAdapter.notifyDataSetChanged();
                if(currentPos != null)
                    refreshMarker(currentPos);
            }
        });

        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.VISIBLE);
                dataLayout.setVisibility(View.GONE);
            }
        });
        setData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.GONE);
                dataLayout.setVisibility(View.VISIBLE);
            }
        });

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);

        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomIn();
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().zoomOut();
            }
        });

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(currentPos == null) {
                        Toast.makeText(AddPOIMarkerActivity.this, R.string.locationRequired, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nameStr = name.getText().toString();
                    String descriptionStr = description.getText().toString();
                    JSONObject obj = new JSONObject();
                    obj.put("lat", currentPos.getLatitude());
                    obj.put("lng", currentPos.getLongitude());
                    String coordinates = obj.toString();
                    API.getApiInterface(AddPOIMarkerActivity.this).savePOIMarker(api_key, getResources().getString(R.string.lang),
                            nameStr, descriptionStr, selectedIcon.id, coordinates, new Callback<ApiInterface.SavePOIMarkerResult>() {
                                @Override
                                public void success(ApiInterface.SavePOIMarkerResult savePOIMarkerResult, Response response) {
                                    Toast.makeText(AddPOIMarkerActivity.this, R.string.poiMarkerAdded, Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(AddPOIMarkerActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AddPOIMarkerActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint)
    {
        currentPos = geoPoint;
        refreshMarker(currentPos);
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    private void refreshMarker(final GeoPoint geoPoint)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                int dp100 = Utils.dpToPx(AddPOIMarkerActivity.this, 50);
                String server_base = (String) DataSaver.getInstance(AddPOIMarkerActivity.this).load("server_base");

                try {
                    Bitmap bmp = BitmapFactory.decodeStream(new URL(server_base + selectedIcon.path).openConnection().getInputStream());

                    int srcWidth = bmp.getWidth();
                    int srcHeight = bmp.getHeight();

                    int maxWidth = dp100;
                    int maxHeight = dp100;

                    float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                    int dstWidth = (int) (srcWidth * ratio);
                    int dstHeight = (int) (srcHeight * ratio);

                    bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);
                    olItem = new OverlayItem("Marker position", "", new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
                    olItem.setMarker(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(overlay == null)
                    overlay = new ItemizedIconOverlay<>(AddPOIMarkerActivity.this, new ArrayList(), null);
                else
                    map.getOverlays().remove(overlay);
                overlay.removeAllItems();
                overlay.addItem(olItem);
                map.getOverlays().add(overlay);
                map.invalidate();
            }
        }.execute();
    }
}
