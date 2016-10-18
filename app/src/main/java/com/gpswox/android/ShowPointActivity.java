package com.gpswox.android;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.gpswox.android.utils.Utils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShowPointActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.latitude) EditText latitude;
    @Bind(R.id.longitude) EditText longitude;
    @Bind(R.id.showPoint) View showPoint;
    @Bind(R.id.zoom_in) View zoom_in;
    @Bind(R.id.zoom_out) View zoom_out;
    @Bind(R.id.map) MapView map;
    @Bind(R.id.mapLayout) View mapLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_point);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        showPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.VISIBLE);

                ItemizedIconOverlay<OverlayItem> mOverlay;
                ArrayList<OverlayItem> items = new ArrayList<>();

                try {
                    double lat = Double.parseDouble(latitude.getText().toString());
                    double lng = Double.parseDouble(longitude.getText().toString());
                    OverlayItem olItem = new OverlayItem(getString(R.string.yourPoint), "", new GeoPoint(lat, lng));
                    Drawable dr = ContextCompat.getDrawable(ShowPointActivity.this, R.drawable.map_simple_marker);
                    Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                    int srcWidth = dr.getIntrinsicWidth();
                    int srcHeight = dr.getIntrinsicHeight();

                    int maxWidth = Utils.dpToPx(ShowPointActivity.this, 100);
                    int maxHeight = Utils.dpToPx(ShowPointActivity.this, 100);

                    float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                    int dstWidth = (int) (srcWidth * ratio);
                    int dstHeight = (int) (srcHeight * ratio);

                    olItem.setMarker(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true)));
                    items.add(olItem);
                    mOverlay = new ItemizedIconOverlay<>(ShowPointActivity.this, items, null);
                    map.getOverlays().add(mOverlay);
                    map.getController().setZoom(10);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

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

    }
}
