package com.gpswox.android;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.TextView;

import com.gpswox.android.models.Device;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.List;

/**
 * Created by gintas on 01/02/16.
 */
public class MapMarkerInfoWindow extends InfoWindow
{
    public MapMarkerInfoWindow(Context context, Device device, int layoutResId, MapView mapView) {
        super(layoutResId, mapView);

        View view = getView();
        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        TextView device_name = (TextView) view.findViewById(R.id.device_name);
        device_name.setText(device.name);
        TextView position = (TextView) view.findViewById(R.id.position);
        position.setText(device.lat + "° " + device.lng + "°");
        TextView time = (TextView) view.findViewById(R.id.time);
        time.setText(device.time);
        TextView speed = (TextView) view.findViewById(R.id.speed);
        speed.setText(device.speed + " aaaaa");
        TextView address = (TextView) view.findViewById(R.id.address);
        address.setText(device.address);

        List<Address> addresses;
        try {
            addresses = new Geocoder(context).getFromLocation(device.lat, device.lng, 1);
            if (addresses.size() > 0)
                address.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //        device.device_data.fue
        //        ProgressBar fuelTank = (ProgressBar) getView().findViewById(R.id.fuelTank);
    }

    @Override
    public void onOpen(Object o) {

    }

    @Override
    public void onClose() {

    }
}
