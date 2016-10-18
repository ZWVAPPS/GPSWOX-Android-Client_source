package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.DeviceIcon;
import com.gpswox.android.models.FuelMeasurement;
import com.gpswox.android.models.ObjectGroup;
import com.gpswox.android.models.Sensor;
import com.gpswox.android.models.Timezone;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditObjectActivity extends AppCompatActivity
{
    private static final String TAG = "EditObjectActivity";
    @Bind(R.id.back) View back;
    @Bind(R.id.action_bar_title) TextView action_bar_title;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;
    @Bind(R.id.saveChanges) View saveChanges;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;

    private BaseExpandableListAdapter adapter;
    private Device device;
    private String api_key;
    private ApiInterface.GetFieldsDataForEditingResult fieldsDataForEditingResult;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_object);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        device = new Gson().fromJson(getIntent().getExtras().getString("device"), Device.class);
        action_bar_title.setText(device.name);

        api_key = (String) DataSaver.getInstance(this).load("api_key");

        adapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return 3;//4;
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
                    case 3: resId = R.string.sensors; break;
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
                        convertView = inflateSensorsItem(); break;
                }

                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return false;
            }
        };
        refresh();

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device == null || fieldsDataForEditingResult == null) return; // not loaded yet
                if(device.name.equals(""))
                    Toast.makeText(EditObjectActivity.this, R.string.nameMustBeSet, Toast.LENGTH_SHORT).show();
                else if(device.device_data.imei.equals(""))
                    Toast.makeText(EditObjectActivity.this, R.string.imeiMustBeSet, Toast.LENGTH_SHORT).show();
                else
                    API.getApiInterface(EditObjectActivity.this).saveEditedDevice(
                        (String) DataSaver.getInstance(EditObjectActivity.this).load("api_key"),
                        Lang.getCurrentLanguage(),
                        device.id,
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
                        new Callback<ApiInterface.SaveEditedDeviceResult>() {
                            @Override
                            public void success(ApiInterface.SaveEditedDeviceResult result, Response response)
                            {
                                Toast.makeText(EditObjectActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Toast.makeText(EditObjectActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });

        Utils.setGroupClickListenerToNotify(expandable_list, adapter);
    }

    private void refresh()
    {
        content_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getFieldsDataForEditing(api_key, Lang.getCurrentLanguage(), 2, new Callback<ApiInterface.GetFieldsDataForEditingResult>() {
            @Override
            public void success(ApiInterface.GetFieldsDataForEditingResult result, Response response)
            {
                fieldsDataForEditingResult = result;

                if(fieldsDataForEditingResult.device_groups == null)
                    fieldsDataForEditingResult.device_groups = new ArrayList<ObjectGroup>();
                expandable_list.setAdapter(adapter);
                loading_layout.setVisibility(View.GONE);
                content_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() == 403) {
                    Toast.makeText(EditObjectActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e("EditObjectActivity", retrofitError.getMessage());
                    Toast.makeText(EditObjectActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
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
                String server = (String) DataSaver.getInstance(EditObjectActivity.this).load("server_base");
                Picasso.with(EditObjectActivity.this).load(server + item.path).into(image);

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
        for (int i = 0; i < fieldsDataForEditingResult.device_groups.size(); i++) {
            ObjectGroup objectGroup = fieldsDataForEditingResult.device_groups.get(i);
            if (objectGroup.id == device.device_data.group_id)
                group.setSelection(i);
        }

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
        for (int i = 0; i < fieldsDataForEditingResult.device_fuel_measurements.size(); i++) {
            FuelMeasurement fuelMeasurement = fieldsDataForEditingResult.device_fuel_measurements.get(i);
            if (fuelMeasurement.id == device.device_data.fuel_measurement_id)
                measurement.setSelection(i);
        }

        // cost per liter
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

        // device model
        EditText deviceModel = (EditText) root.findViewById(R.id.deviceModel);
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
        EditText plateNumber = (EditText) root.findViewById(R.id.plateNumber);
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
        EditText vin = (EditText) root.findViewById(R.id.vin);
        vin.setText(device.device_data.plate_number);
        vin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                device.device_data.vin = s.toString();
            }
        });

        // registration/asset number
        EditText registrationAssetNumber = (EditText) root.findViewById(R.id.registrationAssetNumber);
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

        // object/owner manager
        EditText objectOwnerManager = (EditText) root.findViewById(R.id.objectOwnerManager);
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
        return root;
    }
    private View inflateSensorsItem()
    {
        View root = getLayoutInflater().inflate(R.layout.adapter_editobject_expandable_sensors, null);

        ListView sensors_list = (ListView) root.findViewById(R.id.sensors_list);
        sensors_list.setAdapter(new AwesomeAdapter<Sensor>(this, device.sensors)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_sensor, null);
                final Sensor item = getItem(position);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                name.setText(item.name);
                TextView type = (TextView) convertView.findViewById(R.id.type);
                type.setText(item.type);

                convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        API.getApiInterface(EditObjectActivity.this).destroySensor(api_key, item.id, Lang.getCurrentLanguage(), new Callback<ApiInterface.DestroySensorResult>()
                        {
                            @Override
                            public void success(ApiInterface.DestroySensorResult result, Response response)
                            {
                                remove(item);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Log.e(TAG, "failure: retrofitError" + retrofitError.getMessage());
                                Toast.makeText(EditObjectActivity.this, getString(R.string.errorHappened), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                convertView.findViewById(R.id.gear).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(EditObjectActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
                    }
                });
                return convertView;
            }
        });
        root.findViewById(R.id.addSensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(EditObjectActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }
}
