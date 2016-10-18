package com.gpswox.android.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gpswox.android.R;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.Sensor;
import com.gpswox.android.views.FullHeightListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by gintas on 08/10/15.
 */
public class ObjectInfoAdapter extends BaseExpandableListAdapter
{
    public Context context;
    public Device device;
    public LayoutInflater inflater;


    public ObjectInfoAdapter(Context context, Device device)
    {
        this.context = context;
        this.device = device;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if(convertView == null)
            convertView = inflater.inflate(R.layout.adapter_expandable_parent, null);
        String titleText = "";
        switch(groupPosition)
        {
            case 0: titleText = context.getString(R.string.general); break;
            case 1: titleText = context.getString(R.string.driverInformation); break;
            case 2: titleText = context.getString(R.string.location); break;
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(titleText);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        if(convertView == null)
            convertView = new FullHeightListView(context);

        ListView listview = (ListView) convertView;
        ArrayList<Pair> array = new ArrayList<>();
        if(groupPosition == 0) // general
        {
            array.add(new Pair<>(context.getString(R.string.object)+":", device.name));
            array.add(new Pair<>(context.getString(R.string.model)+":", device.device_data.device_model));
            array.add(new Pair<>(context.getString(R.string.plate)+":", device.device_data.plate_number));
            for(Sensor sensor : device.sensors)
                array.add(new Pair<>(sensor.name+":", sensor.value));
        }
        else if(groupPosition == 1)
        {
            if(device.driver_data.name != null)
                array.add(new Pair<>(context.getString(R.string.name)+":", device.driver_data.name));
            if(device.driver_data.rfid != null)
                array.add(new Pair<>(context.getString(R.string.rfid)+":", device.driver_data.rfid));
            if(device.driver_data.phone != null)
                array.add(new Pair<>(context.getString(R.string.phone)+":", device.driver_data.phone));
            if(device.driver_data.email != null)
                array.add(new Pair<>(context.getString(R.string.email)+":", device.driver_data.email));
            if(device.driver_data.description != null)
                array.add(new Pair<>(context.getString(R.string.description)+":", device.driver_data.description));
        }
        else if(groupPosition == 2)
        {
            array.add(new Pair<>(context.getString(R.string.position), device.lat + "° " + device.lng + "°"));
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(device.lat, device.lng, 1);
                if(addresses.size() > 0)
                {
                    String address = addresses.get(0).getAddressLine(0);
                    array.add(new Pair<>(context.getString(R.string.address), address));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        listview.setAdapter(new AwesomeAdapter<Pair>(context, array)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = inflater.inflate(R.layout.adapter_info_list, null);
                Pair item = getItem(position);
                TextView left = (TextView) convertView.findViewById(R.id.left);
                TextView right = (TextView) convertView.findViewById(R.id.right);

                left.setText(item.first != null ? String.valueOf(item.first) : "");
                right.setText(item.second != null ? String.valueOf(item.second) : "");
                return convertView;
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
