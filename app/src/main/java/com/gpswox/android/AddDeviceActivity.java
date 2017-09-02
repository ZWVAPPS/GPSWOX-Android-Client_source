package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.DeviceIcon;
import com.gpswox.android.models.FuelMeasurement;
import com.gpswox.android.models.ObjectGroup;
import com.gpswox.android.models.Timezone;
import com.gpswox.android.utils.DataSaver;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddDeviceActivity extends AppCompatActivity
{
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;
    @Bind(R.id.add_device) View add_device;
    @Bind(R.id.back) View back;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;

    private BaseExpandableListAdapter adapter;
    private Device device;
    private ApiInterface.GetFieldsDataForEditingResult fieldsDataForEditingResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return 5;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return 1;
            }

            @Override
            public String getGroup(int groupPosition) {
                int resId = 0;
                switch(groupPosition)
                {
                    case 0: resId = R.string.main; break;
                    case 1: resId = R.string.icon; break;
                    case 2: resId = R.string.advanced; break;
                    case 3: resId = R.string.accuracy; break;
                    case 4: resId = R.string.tail; break;
                }
                return getString(resId);
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return null;
            }

            @Override
            public long getGroupId(int groupPosition) {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_expandable_parent, null);

                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(getGroup(groupPosition));

                ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
                expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
            {
                switch(groupPosition)
                {
                    case 0:
                        convertView = inflateMainItem(); break;
                    case 1:
                        convertView = inflateIconsItem(); break;
                    case 2:
                        convertView = inflateAdvancedItem(); break;
                    case 3:
                        convertView = inflateAccuracyItem(); break;
                    case 4:
                        convertView = inflateTailItem(); break;
                }

                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return false;
            }
        };

        refresh();

        add_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device == null || fieldsDataForEditingResult == null) return; // not loaded yet
                if(device.name.equals(""))
                    Toast.makeText(AddDeviceActivity.this, R.string.nameMustBeSet, Toast.LENGTH_SHORT).show();
                else if(device.device_data.imei.equals(""))
                    Toast.makeText(AddDeviceActivity.this, R.string.imeiMustBeSet, Toast.LENGTH_SHORT).show();
                else
                {
                    API.getApiInterface(AddDeviceActivity.this).addNewDevice(
                            (String) DataSaver.getInstance(
                                    AddDeviceActivity.this).load("api_key"),
                                    getResources().getString(R.string.lang),
                                    device.name,
                                    device.device_data.imei,
                                    device.device_data.icon_id,
                                    device.device_data.group_id,
                                    device.device_data.sim_number,
                                    device.device_data.device_model,
                                    device.device_data.plate_number,
                                    device.device_data.vin,
                                    device.device_data.registration_number,
                                    device.device_data.object_owner,
                                    device.device_data.fuel_measurement_id,
                                    device.device_data.fuel_quantity,
                                    device.device_data.fuel_price,
                                    device.device_data.min_moving_speed,
                                    device.device_data.min_fuel_fillings,
                                    device.device_data.min_fuel_thefts,
                                    device.device_data.tail_color,
                                    device.device_data.tail_length,
                                    device.device_data.timezone_id,
                                    new Callback<ApiInterface.AddDeviceResult>() {
                                        @Override
                                        public void success(ApiInterface.AddDeviceResult result, Response response)
                                        {
                                            Toast.makeText(AddDeviceActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                                            finish();
                                        }

                                        @Override
                                        public void failure(RetrofitError retrofitError) {
                                            Toast.makeText(AddDeviceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                    );
                }
            }
        });
    }

    private void refresh()
    {
        content_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getFieldsDataForEditing((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), 0, new Callback<ApiInterface.GetFieldsDataForEditingResult>() {
            @Override
            public void success(ApiInterface.GetFieldsDataForEditingResult result, Response response)
            {
                fieldsDataForEditingResult = result;

                // create device object and set defaults
                device = new Device();
                device.device_data.icon_id = fieldsDataForEditingResult.device_icons.get(0).id;
                device.device_data.min_moving_speed = 6;
                device.device_data.min_fuel_fillings = 10;
                device.device_data.min_fuel_thefts = 10;
                device.device_data.tail_color = "#33cc33";
                device.device_data.tail_length = 5;

                expandable_list.setAdapter(adapter);

                loading_layout.setVisibility(View.GONE);
                content_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() == 403) {
                    Toast.makeText(AddDeviceActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(AddDeviceActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
            }
        });
    }

    private View inflateMainItem()
    {
        View root = getLayoutInflater().inflate(R.layout.adapter_editobject_expandable_main, null);
        EditText name = (EditText) root.findViewById(R.id.name);
        EditText imei = (EditText) root.findViewById(R.id.imei);
        name.setText(device.name);
        imei.setText(device.device_data.imei);

        name.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.name = s.toString();
            }
        });

        imei.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.imei = s.toString();
            }
        });
        return root;
    }
    private View inflateIconsItem() {
        View root = getLayoutInflater().inflate(R.layout.adapter_editobject_expandable_icon, null);
        GridView icons_list = (GridView) root.findViewById(R.id.icons_list);
        final AwesomeAdapter<DeviceIcon> iconsAdapter = new AwesomeAdapter<DeviceIcon>(this) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_editobject_icons, null);

                DeviceIcon item = getItem(position);
                ImageView image = (ImageView) convertView.findViewById(R.id.imageview);
                String server = (String) DataSaver.getInstance(AddDeviceActivity.this).load("server_base");
                Picasso.with(AddDeviceActivity.this).load(server + item.path).into(image);

                if(device.device_data.icon_id == item.id)
                    convertView.setBackgroundResource(R.drawable.selected_icon_border);
                else
                    convertView.setBackgroundColor(0xFFFFFFFF);
                return convertView;
            }
        };
        icons_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceIcon item = iconsAdapter.getItem(position);
                device.device_data.icon_id = item.id;
                iconsAdapter.notifyDataSetChanged();
            }
        });
        iconsAdapter.setArray(fieldsDataForEditingResult.device_icons);
        icons_list.setAdapter(iconsAdapter);
        return root;
    }
    private View inflateAdvancedItem()
    {
        View root = getLayoutInflater().inflate(R.layout.adapter_editobject_expandable_advanced, null);

        // sim number
        final EditText simNo = (EditText) root.findViewById(R.id.simNo);
        simNo.setText(device.device_data.sim_number);
        simNo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.sim_number = s.toString();
            }
        });

        // device model
        final EditText deviceModel = (EditText) root.findViewById(R.id.deviceModel);
        deviceModel.setText(device.device_data.device_model);
        deviceModel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.device_model = s.toString();
            }
        });

        // plate number
        final EditText plateNumber = (EditText) root.findViewById(R.id.plateNumber);
        plateNumber.setText(device.device_data.plate_number);
        plateNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.plate_number = s.toString();
            }
        });

        // vin
        final EditText vin = (EditText) root.findViewById(R.id.vin);
        vin.setText(device.device_data.vin);
        vin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.vin = s.toString();
            }
        });

        // Registration/asset number
        final EditText registrationAssetNumber = (EditText) root.findViewById(R.id.registrationAssetNumber);
        registrationAssetNumber.setText(device.device_data.registration_number);
        registrationAssetNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.registration_number = s.toString();
            }
        });

        // Object owner/manager
        final EditText objectOwnerManager = (EditText) root.findViewById(R.id.objectOwnerManager);
        objectOwnerManager.setText(device.device_data.object_owner);
        objectOwnerManager.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.object_owner = s.toString();
            }
        });

        // group spinner
        Spinner group = (Spinner) root.findViewById(R.id.group);
        final ArrayAdapter<ObjectGroup> groupAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fieldsDataForEditingResult.device_groups);
        group.setAdapter(groupAdapter);
        group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                device.device_data.group_id = groupAdapter.getItem(position).id;
            }
        });

        // measurement spinner
        final Spinner measurement = (Spinner) root.findViewById(R.id.measurement);
        final ArrayAdapter<FuelMeasurement> measurementAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fieldsDataForEditingResult.device_fuel_measurements);
        measurement.setAdapter(measurementAdapter);
        measurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                device.device_data.fuel_measurement_id = measurementAdapter.getItem(position).id;
            }
        });

        // cost for liter
        EditText costForLiter = (EditText) root.findViewById(R.id.costForLiter);
        costForLiter.setText(device.device_data.fuel_price);
        costForLiter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.fuel_price = s.toString();
            }
        });

        // km per liter
        EditText kmPerLiter = (EditText) root.findViewById(R.id.kmPerLiter);
        kmPerLiter.setText(device.device_data.fuel_quantity);
        kmPerLiter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.fuel_quantity = s.toString();
            }
        });

        // time adjustment
        final Spinner timeAdjustment = (Spinner) root.findViewById(R.id.timeAdjustment);
        final ArrayAdapter<Timezone> timezonesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fieldsDataForEditingResult.timezones);
        timeAdjustment.setAdapter(timezonesAdapter);
        timeAdjustment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                device.device_data.timezone_id = timezonesAdapter.getItem(position).id;
            }
        });
        for (int i = 0; i < fieldsDataForEditingResult.timezones.size(); i++) {
            Timezone timezone = fieldsDataForEditingResult.timezones.get(i);
            if (timezone.id == device.device_data.timezone_id)
                timeAdjustment.setSelection(i);
        }
        return root;
    }

    private View inflateAccuracyItem()
    {
        View root = getLayoutInflater().inflate(R.layout.adapter_addobject_expandable_accuracy, null);
        EditText minMovingSpeedKmh = (EditText) root.findViewById(R.id.minMovingSpeedKmh);
        minMovingSpeedKmh.setText(String.valueOf(device.device_data.min_moving_speed));
        minMovingSpeedKmh.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.min_moving_speed = Integer.valueOf(s.toString());
            }
        });
        EditText minFuelDifferenceFillings = (EditText) root.findViewById(R.id.minFuelDifferenceFillings);
        minFuelDifferenceFillings.setText(String.valueOf(device.device_data.min_fuel_fillings));
        minFuelDifferenceFillings.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.min_fuel_fillings = Integer.valueOf(s.toString());
            }
        });
        EditText minFuelDifferenceTheft = (EditText) root.findViewById(R.id.minFuelDifferenceTheft);
        minFuelDifferenceTheft.setText(String.valueOf(device.device_data.min_fuel_thefts));
        minFuelDifferenceTheft.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.min_fuel_thefts = Integer.valueOf(s.toString());
            }
        });
        return root;
    }

    private View inflateTailItem()
    {
        View root = getLayoutInflater().inflate(R.layout.adapter_addobject_expandable_tail, null);
        EditText tailColor = (EditText) root.findViewById(R.id.tailColor);
        tailColor.setText(device.device_data.tail_color);
        tailColor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.tail_color = s.toString();
            }
        });

        EditText tailLength = (EditText) root.findViewById(R.id.tailLength);
        tailLength.setText(String.valueOf(device.device_data.tail_length));
        tailLength.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.tail_length = Integer.valueOf(s.toString());
            }
        });
        return root;
    }
}
