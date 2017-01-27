package com.gpswox.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.EditObjectActivity;
import com.gpswox.android.ObjectInfoActivity;
import com.gpswox.android.R;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by gintas on 08/10/15.
 */
public class ObjectsAdapter extends BaseExpandableListAdapter
{
    public Context context;
    public ArrayList<ApiInterface.GetDevicesItem> array;
    public LayoutInflater inflater;


    public ArrayList<ApiInterface.GetDevicesItem> original;

    public ObjectsAdapter(Context context, ArrayList<ApiInterface.GetDevicesItem> array)
    {
        this.context = context;
        this.array = array;
        this.original = array;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return array.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return array.get(groupPosition).items.size();
    }

    @Override
    public ApiInterface.GetDevicesItem getGroup(int groupPosition) {
        return array.get(groupPosition);
    }

    @Override
    public Device getChild(int groupPosition, int childPosition) {
        return array.get(groupPosition).items.get(childPosition);
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
        ApiInterface.GetDevicesItem item = getGroup(groupPosition);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.title + " (" + item.items.size() + ")");

        ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
        expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        // checkbox setChecked doesn't work, if we don't reinflate it.
        convertView = inflater.inflate(R.layout.adapter_objects_child, null);

        final Device item = getChild(groupPosition, childPosition);

        DataSaver.getInstance(context).save("unit_of_distance", item.distance_unit_hour);
        DataSaver.getInstance(context).save("unit_of_capacity", item.unit_of_capacity);
        DataSaver.getInstance(context).save("unit_of_altitude", item.unit_of_altitude);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.name);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        if(item.timestamp != 0)
            date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(item.timestamp * 1000)));
        TextView speed = (TextView) convertView.findViewById(R.id.speed);
        speed.setText(item.speed + " " + item.distance_unit_hour);
        TextView altitudeValue = (TextView) convertView.findViewById(R.id.altitudeValue);
        altitudeValue.setText(item.altitude+ " " + DataSaver.getInstance(context).load("unit_of_altitude"));
        TextView protocolValue = (TextView) convertView.findViewById(R.id.protocolValue);
        protocolValue.setText(item.protocol);
        TextView driverValue = (TextView) convertView.findViewById(R.id.driverValue);
        driverValue.setText(item.driver_data.name != null ? item.driver_data.name : "");
        TextView positionValue = (TextView) convertView.findViewById(R.id.positionValue);
        positionValue.setText(item.lat + "° " + item.lng + "°");

        final CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
        checkbox.setChecked(item.device_data.active == 1);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked)
            {
                API.getApiInterface(context).changeActiveDevice((String) DataSaver.getInstance(context).load("api_key"), Lang.getCurrentLanguage(), item.id, isChecked, new Callback<ApiInterface.ChangeActiveDeviceResult>() {
                    @Override
                    public void success(ApiInterface.ChangeActiveDeviceResult changeActiveDeviceResult, Response response) {
                        item.device_data.active = item.device_data.active == 1 ? 0 : 1;
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(context, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        checkbox.setChecked(!isChecked);
                    }
                });
            }
        });

        if(item.online.equals("online"))
        {
            ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
            onlineImageView.setImageResource(R.drawable.green_dot);
        }
        else if(item.online.equals("offline"))
        {
            ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
            onlineImageView.setImageResource(R.drawable.red_dot);
        }
        else if(item.online.equals("ack"))
        {
            ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
            onlineImageView.setImageResource(R.drawable.yellow_dot);
        }

        final View additionalLayout = convertView.findViewById(R.id.additionalLayout);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(additionalLayout.getVisibility() == View.VISIBLE)
                    additionalLayout.setVisibility(View.GONE);
                else
                   additionalLayout.setVisibility(View.VISIBLE);
            }
        });

        convertView.findViewById(R.id.gear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditObjectActivity.class);
                intent.putExtra("device", new Gson().toJson(item));
                context.startActivity(intent);
            }
        });

        convertView.findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ObjectInfoActivity.class);
                intent.putExtra("device", new Gson().toJson(item));
                context.startActivity(intent);
            }
        });
        if(childPosition != 0) convertView.findViewById(R.id.shadow).setVisibility(View.INVISIBLE);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    ItemFilter mFilter = new ItemFilter();
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final ArrayList<ApiInterface.GetDevicesItem> nlist = new ArrayList<>();
            for(ApiInterface.GetDevicesItem item : original)
                nlist.add(new ApiInterface.GetDevicesItem(item));

            Iterator<ApiInterface.GetDevicesItem> it = nlist.iterator();
            while (it.hasNext())
            {
                ApiInterface.GetDevicesItem item = it.next();

                Iterator<Device> it2 = item.items.iterator();
                while (it2.hasNext())
                {
                    Device device = it2.next();
                    if(!device.fitForFilter(filterString))
                        it2.remove();
                }

                if(item.items.size() == 0)
                    it.remove();
            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            array = (ArrayList<ApiInterface.GetDevicesItem>) results.values;
            notifyDataSetChanged();
        }

    }
}
