package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.models.HistoryItemCoord;
import com.gpswox.android.models.HistorySensor;
import com.gpswox.android.models.HistorySensorData;
import com.gpswox.android.utils.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryItemCoordDetailsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap map;
    @Bind(R.id.back) View back;
    @Bind(R.id.listview) ListView listview;
    @Bind(R.id.bottomButton) Button bottomButton;
    @Bind(R.id.map) View map_layout;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;
    @Bind(R.id.zoom_container) View zoom_container;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item_coord_details);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        HistoryItemCoord item = new Gson().fromJson(getIntent().getStringExtra("item"), HistoryItemCoord.class);
        ArrayList<HistorySensor> sensors = new Gson().fromJson(getIntent().getStringExtra("sensors"), new TypeToken<ArrayList<HistorySensor>>(){}.getType());

        ArrayList<Pair> array = new ArrayList<>();
        array.add(new Pair<>(getString(R.string.time)+":", item.raw_time));
        array.add(new Pair<>(getString(R.string.latitude)+":", item.lat));
        array.add(new Pair<>(getString(R.string.longitude)+":", item.lng));
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocation(Double.valueOf(item.lat), Double.valueOf(item.lng), 1);
            if(addresses.size() > 0)
            {
                array.add(new Pair<>(getString(R.string.address) + ":", addresses.get(0).getAddressLine(0)));
            }
        } catch (IOException e) { }

        for(HistorySensorData sensor : item.sensors_data)
            for(HistorySensor s : sensors)
                if(s.id.equals(sensor.id))
                    array.add(new Pair<>(s.name + ":", sensor.value + s.sufix));

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(item.other)));
            doc.getDocumentElement().normalize();

            NodeList nodeList=doc.getElementsByTagName("*");
            for (int i=0; i<nodeList.getLength(); i++)
            {
                // Get element
                Element element = (Element)nodeList.item(i);
                if(!element.getNodeName().equals("info"))
                    array.add(new Pair<>(element.getNodeName(), element.getTextContent()));
            }
        } catch(Exception e) {}

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


        // Marker
        Drawable dr = getResources().getDrawable(R.drawable.ruler_marker);
        Bitmap bmp = ((BitmapDrawable) dr).getBitmap();

        int srcWidth = bmp.getWidth();
        int srcHeight = bmp.getHeight();

        int maxWidth = Utils.dpToPx(HistoryItemCoordDetailsActivity.this, 10);
        int maxHeight = Utils.dpToPx(HistoryItemCoordDetailsActivity.this, 10);
        int dp100 = Utils.dpToPx(HistoryItemCoordDetailsActivity.this, 50);

        float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
        int dstWidth = (int) (srcWidth * ratio);
        int dstHeight = (int) (srcHeight * ratio);

        bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(Double.parseDouble(item.lat), Double.parseDouble(item.lng)));
        mo.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
        mo.title(item.lat + ", " + item.lng);
        Marker m = map.addMarker(mo);
        m.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15));
        map_layout.setVisibility(View.GONE);

        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listview.getVisibility() == View.VISIBLE)
                {
                    listview.setVisibility(View.GONE);
                    map_layout.setVisibility(View.VISIBLE);
                    zoom_container.setVisibility(View.VISIBLE);
                    bottomButton.setText(R.string.viewDetails);
                }
                else
                {
                    listview.setVisibility(View.VISIBLE);
                    map_layout.setVisibility(View.GONE);
                    zoom_container.setVisibility(View.GONE);
                    bottomButton.setText(R.string.viewMap);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
    }
}