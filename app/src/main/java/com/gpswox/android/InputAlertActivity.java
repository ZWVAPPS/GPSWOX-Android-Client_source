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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Alert;
import com.gpswox.android.models.AlertDevice;
import com.gpswox.android.models.AlertDistance;
import com.gpswox.android.models.AlertDriver;
import com.gpswox.android.models.AlertEventProtocol;
import com.gpswox.android.models.AlertEventType;
import com.gpswox.android.models.AlertGeofence;
import com.gpswox.android.models.AlertSavedEvent;
import com.gpswox.android.models.AlertSavedGeofence;
import com.gpswox.android.models.AlertZone;
import com.gpswox.android.models.CustomEventByProtocol;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

public class InputAlertActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.saveAlert) View saveAlert;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;

    Alert alert;
    ApiInterface.GetAlertDataResult data;
    BaseExpandableListAdapter adapter;
    ArrayList<ApiInterface.GetProtocolsResult.GetProtocolsResultItem> protocols;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_alert);
        ButterKnife.bind(this);

        alert = new Alert();

        if(getIntent().hasExtra("alert"))
            alert = new Gson().fromJson(getIntent().getStringExtra("alert"), Alert.class);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return 6;
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
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_expandable_parent, null);
                String titleText = "";
                switch(groupPosition)
                {
                    case 0: titleText = getString(R.string.userInfo); break;
                    case 1: titleText = getString(R.string.devices); break;
                    case 2: titleText = getString(R.string.drivers); break;
                    case 3: titleText = getString(R.string.geofencing); break;
                    case 4: titleText = getString(R.string.overspeed); break;
                    case 5: titleText = getString(R.string.events); break;
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(titleText);

                ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
                expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                if(groupPosition == 0)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_userinfo, null);
                    EditText name = (EditText) convertView.findViewById(R.id.name);
                    name.setText(alert.name);
                    name.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void afterTextChanged(Editable s) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            alert.name = s.toString();
                        }
                    });
                    EditText email = (EditText) convertView.findViewById(R.id.email);
                    email.setText(alert.email);
                    email.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void afterTextChanged(Editable s) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            alert.email = s.toString();
                        }
                    });
                    EditText phone = (EditText) convertView.findViewById(R.id.phone);
                    phone.setText(alert.mobile_phone);
                    phone.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void afterTextChanged(Editable s) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            alert.mobile_phone = s.toString();
                        }
                    });

                }
                else if(groupPosition == 1)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_devices, null);
                    ListView devices_list = (ListView) convertView.findViewById(R.id.devices_list);
                    devices_list.setAdapter(new AwesomeAdapter<AlertDevice>(InputAlertActivity.this, data.devices)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            convertView = getLayoutInflater().inflate(R.layout.adapter_sendcommand_devices, null);
                            final AlertDevice item = getItem(position);
                            CheckBox device = (CheckBox) convertView.findViewById(R.id.device);
                            device.setText(item.value);
                            device.setChecked(false);
                            device.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                {
                                    if(isChecked) {
                                        if(!alert.devices.contains(item.id))
                                            alert.devices.add(item.id);
                                    }
                                    else
                                    {
                                        for (int i = 0; i < alert.devices.size(); i++) {
                                            if(alert.devices.get(i) == item.id)
                                                alert.devices.remove(i);
                                        }
                                    }
                                }
                            });
                            for(Integer id : alert.devices)
                                if(id == item.id)
                                    device.setChecked(true);
                            return convertView;
                        }
                    });
                }
                else if(groupPosition == 2)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_devices, null);
                    ListView devices_list = (ListView) convertView.findViewById(R.id.devices_list);
                    devices_list.setAdapter(new AwesomeAdapter<AlertDriver>(InputAlertActivity.this, data.drivers)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            convertView = getLayoutInflater().inflate(R.layout.adapter_sendcommand_devices, null);
                            final AlertDriver item = getItem(position);
                            CheckBox device = (CheckBox) convertView.findViewById(R.id.device);
                            device.setText(item.value);
                            device.setChecked(false);
                            device.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                {
                                    if(isChecked) {
                                        if(!alert.drivers.contains(item.id))
                                            alert.drivers.add(item.id);
                                    }
                                    else
                                    {
                                        for (int i = 0; i < alert.drivers.size(); i++) {
                                            if(alert.drivers.get(i) == item.id)
                                                alert.drivers.remove(i);
                                        }
                                    }
                                }
                            });
                            for(Integer id : alert.drivers)
                                if(id == item.id)
                                    device.setChecked(true);
                            return convertView;
                        }
                    });
                }
                else if(groupPosition == 3)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_geofencing, null);
                    ListView geofences_list = (ListView) convertView.findViewById(R.id.geofences_list);
                    final AwesomeAdapter<AlertSavedGeofence> geofencesApdater = new AwesomeAdapter<AlertSavedGeofence>(InputAlertActivity.this)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            if(convertView == null)
                                convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_savedgeofences, null);
                            final AlertSavedGeofence item = getItem(position);
                            TextView text = (TextView) convertView.findViewById(R.id.text);

                            String geofenceName = "";
                            for(AlertGeofence geofence : data.geofences)
                                if(geofence.id == item.id)
                                    geofenceName = geofence.value;
                            String zoneName = "";
                            for(AlertZone zone : data.alert_zones)
                                if(zone.id == item.zone)
                                    zoneName = zone.value;
                            text.setText(geofenceName + " - " + zoneName);

                            convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.geofences.remove(item);
                                    remove(item);
                                }
                            });
                            return convertView;
                        }
                    };
                    geofencesApdater.setArray(alert.geofences);
                    geofences_list.setAdapter(geofencesApdater);

                    final Spinner geofence_name = (Spinner) convertView.findViewById(R.id.geofence_name);
                    final ArrayAdapter<AlertGeofence> geofencesAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, data.geofences);
                    geofence_name.setAdapter(geofencesAdapter);

                    final Spinner zone_name = (Spinner) convertView.findViewById(R.id.zone_name);
                    final ArrayAdapter<AlertZone> zonesAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, data.alert_zones);
                    zone_name.setAdapter(zonesAdapter);

                    convertView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(geofencesAdapter.getCount() > 0)
                            {
                                AlertSavedGeofence item = new AlertSavedGeofence();
                                item.id = ((AlertGeofence) geofence_name.getSelectedItem()).id;
                                item.zone = ((AlertZone) zone_name.getSelectedItem()).id;
                                alert.geofences.add(item);
                                geofencesApdater.add(item);
                            }
                            else
                            {
                                Toast.makeText(InputAlertActivity.this, R.string.noGeofencingData, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
                else if(groupPosition == 4) {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_overspeed, null);
                    final EditText overspeed = (EditText) convertView.findViewById(R.id.overspeed);
                    overspeed.setText(String.valueOf(alert.overspeed_speed));
                    overspeed.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void afterTextChanged(Editable s) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count)
                        {
                            if(StringUtils.isNotEmpty(s))
                            alert.overspeed_speed = Integer.valueOf(s.toString());
                        }
                    });

                    Spinner unitOfDistance = (Spinner) convertView.findViewById(R.id.unitOfDistance);
                    final ArrayAdapter<AlertDistance> unitOfDistanceAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, data.alert_distance);
                    unitOfDistance.setAdapter(unitOfDistanceAdapter);
                    unitOfDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            alert.overspeed_distance = unitOfDistanceAdapter.getItem(position).id;
                        }
                    });
                    for (int i = 0; i < data.alert_distance.size(); i++) {
                        if(data.alert_distance.get(i).id == alert.overspeed_distance)
                            unitOfDistance.setSelection(i);
                    }
                }
                else if(groupPosition == 5) {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_expandable_events, null);
                    ListView list = (ListView) convertView.findViewById(R.id.list);
                    final Spinner types = (Spinner) convertView.findViewById(R.id.types);
                    final Spinner eventId = (Spinner) convertView.findViewById(R.id.eventId);

                    final AwesomeAdapter<AlertSavedEvent> savedEventsAdapter = new AwesomeAdapter<AlertSavedEvent>(InputAlertActivity.this, alert.events_custom)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            if(convertView == null)
                                convertView = getLayoutInflater().inflate(R.layout.adapter_inputalert_savedevents, null);

                            final AlertSavedEvent item = getItem(position);
                            TextView text = (TextView) convertView.findViewById(R.id.text);

                            String typeName = "";
                            for(AlertEventType type : data.event_types)
                                if(type.id == item.type)
                                    typeName = type.value;
                            text.setText(typeName + " - " + item.protocol + " - " + item.message);

                            convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.events_custom.remove(item);
                                    remove(item);
                                }
                            });
                            return convertView;
                        }
                    };
                    list.setAdapter(savedEventsAdapter);
                    ArrayList listas = protocols.get(0).items;
                    final Spinner protocol = (Spinner) convertView.findViewById(R.id.protocol);
                    final ArrayAdapter<AlertEventProtocol> protocolAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, protocols.get(0).items);
                    protocol.setAdapter(protocolAdapter);
                    protocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if((protocol.getSelectedItem() != null)) {
                                int type = ((AlertEventType) types.getSelectedItem()).id;
                                String protocolId = ((AlertEventProtocol) protocol.getSelectedItem()).id;
                                updateCustomEventsSpinner(eventId, type, protocolId);
                            }
                            else if(eventId.getAdapter() != null)
                                ((ArrayAdapter<CustomEventByProtocol>)eventId.getAdapter()).clear();
                        }
                    });

                    final ArrayAdapter<AlertEventType> typesAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, data.event_types);
                    types.setAdapter(typesAdapter);
                    types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            final ArrayAdapter<AlertEventProtocol> protocolAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, protocols.get(position).items);
                            protocol.setAdapter(protocolAdapter);

                            if((protocol.getSelectedItem() != null)) {
                                int type = ((AlertEventType) types.getSelectedItem()).id;
                                String protocolId = ((AlertEventProtocol) protocol.getSelectedItem()).id;
                                updateCustomEventsSpinner(eventId, type, protocolId);
                            }
                            else if(eventId.getAdapter() != null)
                                ((ArrayAdapter<CustomEventByProtocol>)eventId.getAdapter()).clear();
                        }
                    });

                    if((protocol.getSelectedItem() != null))
                    {
                        int type = ((AlertEventType) types.getSelectedItem()).id;
                        String protocolId = ((AlertEventProtocol) protocol.getSelectedItem()).id;
                        updateCustomEventsSpinner(eventId, type, protocolId);
                    }

                    convertView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(eventId.getSelectedItem() == null) return; // dar neužsikrovė paskutinis dropdown'as
                            AlertSavedEvent item = new AlertSavedEvent();
                            item.id = ((CustomEventByProtocol) eventId.getSelectedItem()).id;
                            item.message = ((CustomEventByProtocol) eventId.getSelectedItem()).value;
                            item.type = ((AlertEventType) types.getSelectedItem()).id;
                            item.protocol = ((AlertEventProtocol) protocol.getSelectedItem()).id;
                            alert.events_custom.add(item);
                            savedEventsAdapter.add(item);
                            savedEventsAdapter.notifyDataSetChanged();
                        }
                    });
                }
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return false;
            }
        };
        Utils.setGroupClickListenerToNotify(expandable_list, adapter);


        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getAlertData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetAlertDataResult>() {
            @Override
            public void success(final ApiInterface.GetAlertDataResult getAlertDataResult, Response response)
            {
                API.getApiInterface(InputAlertActivity.this).getProtocolsForAlertData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetProtocolsResult>() {
                    @Override
                    public void success(ApiInterface.GetProtocolsResult getProtocolsResult, Response response) {
                        loading_layout.setVisibility(View.GONE);
                        content_layout.setVisibility(View.VISIBLE);
                        data = getAlertDataResult;
                        protocols = getProtocolsResult.items;
                        expandable_list.setAdapter(adapter);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(InputAlertActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() == 403) {
                    Toast.makeText(InputAlertActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(InputAlertActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
            }
        });

        saveAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(alert.name.equals(""))
                {
                    Toast.makeText(InputAlertActivity.this, R.string.nameMustBeSet, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(alert.devices.size() <= 0)
                {
                    Toast.makeText(InputAlertActivity.this, R.string.mustSelectMinOneDevice, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(getIntent().hasExtra("alert")) // was editing
                {
                    String devices_array = new Gson().toJson(alert.devices);
                    String drivers_array = new Gson().toJson(alert.drivers);
                    String geofences_array = new Gson().toJson(alert.geofences);
                    String events_custom_array = new Gson().toJson(alert.events_custom);
                    API.getApiInterface(InputAlertActivity.this).saveEditedAlert(api_key, Lang.getCurrentLanguage(), alert.id, alert.name, alert.email, devices_array, drivers_array, geofences_array, alert.overspeed_speed, alert.overspeed_distance, events_custom_array, new Callback<ApiInterface.SaveEditedAlertResult>() {
                        @Override
                        public void success(ApiInterface.SaveEditedAlertResult saveEditedAlertResult, Response response) {
                            Toast.makeText(InputAlertActivity.this, R.string.eventSaved, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(InputAlertActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else // adding new
                {
                    String devices_array = new Gson().toJson(alert.devices);
                    String drivers_array = new Gson().toJson(alert.drivers);
                    String geofences_array = new Gson().toJson(alert.geofences);
                    String events_custom_array = new Gson().toJson(alert.events_custom);
                    API.getApiInterface(InputAlertActivity.this).addNewAlert(api_key, Lang.getCurrentLanguage(), alert.name, alert.email, devices_array, drivers_array, geofences_array, alert.overspeed_speed, alert.overspeed_distance, events_custom_array, new Callback<ApiInterface.AddNewAlertResult>() {
                        @Override
                        public void success(ApiInterface.AddNewAlertResult addNewAlertResult, Response response) {
                            Toast.makeText(InputAlertActivity.this, R.string.eventAdded, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(InputAlertActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void updateCustomEventsSpinner(final Spinner eventIdSpinner, int type, String protocol)
    {
        if(eventIdSpinner.getAdapter() != null)
            ((ArrayAdapter<CustomEventByProtocol>)eventIdSpinner.getAdapter()).clear();
        API.getApiInterface(this).getEventsByProtocolForDropdown((String) DataSaver.getInstance(this).load("api_key"), Lang.getCurrentLanguage(), type, protocol, new Callback<ApiInterface.CustomEventsByProtocol>() {
            @Override
            public void success(ApiInterface.CustomEventsByProtocol array, Response response)
            {
                Log.d("getEventsByProtocol Res", response.getBody().toString());
                /*ArrayAdapter<CustomEventByProtocol> eventIdAdapter = new ArrayAdapter<>(InputAlertActivity.this, R.layout.spinner_item, array);
                eventIdSpinner.setAdapter(eventIdAdapter);*/
                // todo
                TypedInput body = response.getBody();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
                    StringBuilder out = new StringBuilder();
                    String newLine = System.getProperty("line.separator");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                        out.append(newLine);
                    }

                    // Prints the correct String representation of body.
                    System.out.println(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(InputAlertActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
