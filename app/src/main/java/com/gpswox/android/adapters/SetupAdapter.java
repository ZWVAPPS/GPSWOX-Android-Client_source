package com.gpswox.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
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
import com.gpswox.android.AddCustomEventActivity;
import com.gpswox.android.AddDriverActivity;
import com.gpswox.android.InputGprsTemplateActivity;
import com.gpswox.android.InputSmsTemplateActivity;
import com.gpswox.android.R;
import com.gpswox.android.SendTestSmsActivity;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.CustomEvent;
import com.gpswox.android.models.Driver;
import com.gpswox.android.models.ObjectGroup;
import com.gpswox.android.models.SetupData;
import com.gpswox.android.models.SmsAuthenticationSelect;
import com.gpswox.android.models.SmsGatewayEncoding;
import com.gpswox.android.models.SmsGatewayRequestMethod;
import com.gpswox.android.models.Timezone;
import com.gpswox.android.models.UnitOfAltitude;
import com.gpswox.android.models.UnitOfCapacity;
import com.gpswox.android.models.UnitOfDistance;
import com.gpswox.android.models.UserGprsTemplate;
import com.gpswox.android.models.UserSmsTemplate;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by gintas on 29/11/15.
 */
public class SetupAdapter extends BaseExpandableListAdapter {

    private Context context;
    public ListView listview;
    private LayoutInflater inflater;
    private ApiInterface.SetupDataResult setupDataResult;
    private SetupData setupData;
    private ArrayList<Driver> drivers;
    private ArrayList<CustomEvent> events;
    private ArrayList<UserSmsTemplate> smsTemplates;
    private ArrayList<UserGprsTemplate> gprsTemplates;
    public SetupAdapter(Context context, ExpandableListView listview, ApiInterface.SetupDataResult setupDataResult,
                        ArrayList<Driver> drivers,
                        ArrayList<CustomEvent> events,
                        ArrayList<UserSmsTemplate> smsTemplates,
                        ArrayList<UserGprsTemplate> gprsTemplates)
    {
        this.context = context;
        this.listview = listview;
        this.setupDataResult = setupDataResult;
        this.setupData = setupDataResult.item;
        this.drivers = drivers;
        this.events = events;
        this.smsTemplates = smsTemplates;
        this.gprsTemplates = gprsTemplates;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return 7;
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
            convertView = inflater.inflate(R.layout.adapter_expandable_parent, null);
        String titleText = "";
        switch(groupPosition)
        {
            case 0: titleText = context.getString(R.string.main); break;
            case 1: titleText = context.getString(R.string.objectGroups); break;
            case 2: titleText = context.getString(R.string.drivers); break;
            case 3: titleText = context.getString(R.string.events); break;
            case 4: titleText = context.getString(R.string.sms_gateway); break;
            case 5: titleText = context.getString(R.string.sms_templates); break;
            case 6: titleText = context.getString(R.string.gprsTemplates); break;
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(titleText);

        ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
        expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String api_key = (String) DataSaver.getInstance(context).load("api_key");
        if(groupPosition == 0)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_main, null);
            Spinner unitsOfDistance = (Spinner) convertView.findViewById(R.id.unitsOfDistance);
            final ArrayAdapter<UnitOfDistance> unitsOfDistanceAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.units_of_distance);
            unitsOfDistance.setAdapter(unitsOfDistanceAdapter);
            unitsOfDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setupData.unit_of_distance = unitsOfDistanceAdapter.getItem(position).id;
                }
            });
            for(int i = 0; i < setupDataResult.units_of_distance.size(); i++) {
                UnitOfDistance item = setupDataResult.units_of_distance.get(i);
                if (item.id.equals(setupData.unit_of_distance))
                    unitsOfDistance.setSelection(i);
            }

            Spinner unitsOfCapacity = (Spinner) convertView.findViewById(R.id.unitsOfCapacity);
            final ArrayAdapter<UnitOfCapacity> unitsOfCapacityAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.units_of_capacity);
            unitsOfCapacity.setAdapter(unitsOfCapacityAdapter);
            unitsOfCapacity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setupData.unit_of_capacity = unitsOfCapacityAdapter.getItem(position).id;
                }
            });
            for(int i = 0; i < setupDataResult.units_of_capacity.size(); i++) {
                UnitOfCapacity item = setupDataResult.units_of_capacity.get(i);
                if (item.id.equals(setupData.unit_of_capacity))
                    unitsOfCapacity.setSelection(i);
            }

            Spinner unitsOfAltitude = (Spinner) convertView.findViewById(R.id.unitsOfAltitude);
            final ArrayAdapter<UnitOfAltitude> unitsOfAltitudeAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.units_of_altitude);
            unitsOfAltitude.setAdapter(unitsOfAltitudeAdapter);
            unitsOfAltitude.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setupData.unit_of_altitude = unitsOfAltitudeAdapter.getItem(position).id;
                }
            });
            for(int i = 0; i < setupDataResult.units_of_altitude.size(); i++) {
                UnitOfAltitude item = setupDataResult.units_of_altitude.get(i);
                if (item.id.equals(setupData.unit_of_altitude))
                    unitsOfAltitude.setSelection(i);
            }

            Spinner timezone = (Spinner) convertView.findViewById(R.id.timezone);
            final ArrayAdapter<Timezone> timezonesAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.timezones);
            timezone.setAdapter(timezonesAdapter);
            timezone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setupData.timezone_id = timezonesAdapter.getItem(position).id;
                }
            });
            for(int i = 0; i < setupDataResult.timezones.size(); i++) {
                Timezone item = setupDataResult.timezones.get(i);
                if (item.id == setupData.timezone_id)
                    timezone.setSelection(i);
            }
        }
        else if(groupPosition == 1)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_objectgroups, null);
            ListView list = (ListView) convertView.findViewById(R.id.list);
            final EditText newObjectGroupEditText = (EditText) convertView.findViewById(R.id.newObjectGroupEditText);
            final AwesomeAdapter<ObjectGroup> groupsAdapter = new AwesomeAdapter<ObjectGroup>(context, setupDataResult.groups)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null)
                        convertView = inflater.inflate(R.layout.adapter_objectgroups, null);

                    final ObjectGroup item = getItem(position);
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(item.title);

                    View delete = convertView.findViewById(R.id.delete);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            setupDataResult.groups.remove(item);
                            SetupAdapter.this.notifyDataSetChanged();
                        }
                    });
                    return convertView;
                }
            };
            list.setAdapter(groupsAdapter);
            convertView.findViewById(R.id.addNewObjectGroup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    ObjectGroup item = new ObjectGroup();
                    item.title = newObjectGroupEditText.getText().toString();
                    setupDataResult.groups.add(item);
                    SetupAdapter.this.notifyDataSetChanged();

                    newObjectGroupEditText.setText("");
                }
            });
        }
        else if(groupPosition == 2)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_drivers, null);
            final ListView list = (ListView) convertView.findViewById(R.id.list);
            AwesomeAdapter<Driver> adapter = new AwesomeAdapter<Driver>(context, drivers)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.adapter_drivers, null);

                    final Driver item = getItem(position);
                    TextView name = (TextView) convertView.findViewById(R.id.name);
                    name.setText(item.name);
                    TextView description = (TextView) convertView.findViewById(R.id.description);
                    description.setText(item.description);
                    TextView phone = (TextView) convertView.findViewById(R.id.phoneValue);
                    phone.setText(item.phone);
                    TextView email = (TextView) convertView.findViewById(R.id.emailValue);
                    email.setText(item.email);
                    TextView rfid = (TextView) convertView.findViewById(R.id.rfidValue);
                    rfid.setText(item.rfid);
                    TextView device = (TextView) convertView.findViewById(R.id.deviceValue);
                    if(item.device != null)
                        device.setText(item.device.name);
                    else
                        device.setText("");

                    final ImageView info = (ImageView) convertView.findViewById(R.id.info);
                    final View additionalLayout = convertView.findViewById(R.id.additionalLayout);
                    info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(additionalLayout.getVisibility() == View.VISIBLE) {
                                additionalLayout.setVisibility(View.GONE);
                                info.setImageResource(R.drawable.info_off);
                            }
                            else {
                                additionalLayout.setVisibility(View.VISIBLE);
                                info.setImageResource(R.drawable.info_on);
                            }
                            // TODO: reik invaliduot kazkaip, nes height susipisa, requestlayout ir invalidate netinka...
                        }
                    });

                    convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            API.getApiInterface(getContext()).destroyUserDriver((String) DataSaver.getInstance(getContext()).load("api_key"), Lang.getCurrentLanguage(), item.id, new Callback<ApiInterface.DestroyUserDriverResult>() {
                                @Override
                                public void success(ApiInterface.DestroyUserDriverResult destroyUserDriverResult, Response response) {
                                    remove(item);
                                    setupDataResult.groups.remove(item);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(getContext(), R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return convertView;
                }
            };
            list.setAdapter(adapter);

            convertView.findViewById(R.id.addDriver).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    ((Activity) context).startActivityForResult(new Intent(context, AddDriverActivity.class), 3);
                }
            });
        }
        else if(groupPosition == 3)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_events, null);
            ListView list = (ListView) convertView.findViewById(R.id.list);
            AwesomeAdapter<CustomEvent> adapter = new AwesomeAdapter<CustomEvent>(context, events)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    if(convertView == null)
                        convertView = getLayoutInflater().inflate(R.layout.adapter_customevents, null);

                    final CustomEvent item = getItem(position);
                    TextView protocol = (TextView) convertView.findViewById(R.id.protocol);
                    protocol.setText(item.protocol);
                    TextView message = (TextView) convertView.findViewById(R.id.message);
                    message.setText(item.message);

                    /*final ImageView info = (ImageView) convertView.findViewById(R.id.info);
                    final View additionalLayout = convertView.findViewById(R.id.additionalLayout);
                    info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(additionalLayout.getVisibility() == View.VISIBLE) {
                                additionalLayout.setVisibility(View.GONE);
                                info.setImageResource(R.drawable.info_off);
                            }
                            else {
                                additionalLayout.setVisibility(View.VISIBLE);
                                info.setImageResource(R.drawable.info_on);
                            }
                            // TODO: reik invaliduot kazkaip, nes height susipisa, requestlayout ir invalidate netinka...
                        }
                    });*/

                    convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            API.getApiInterface(getContext()).destroyCustomEvent((String) DataSaver.getInstance(getContext()).load("api_key"), Lang.getCurrentLanguage(), item.id, new Callback<ApiInterface.DestroyCustomEventResult>() {
                                @Override
                                public void success(ApiInterface.DestroyCustomEventResult destroyCustomEventResult, Response response) {
                                    remove(item);
                                    events.remove(item);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(getContext(), R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return convertView;
                }
            };
            list.setAdapter(adapter);

            convertView.findViewById(R.id.addEvent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) context).startActivityForResult(new Intent(context, AddCustomEventActivity.class), 4);
                }
            });
        }
        else if(groupPosition == 4)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_smsgateway, null);

            //final View requestMethodLayout = convertView.findViewById(R.id.requestMethodLayout);
            final Spinner requestMethod = (Spinner) convertView.findViewById(R.id.requestMethod);

            // sms gateway url
            setupData.sms_gateway_params.sms_gateway_url = setupData.sms_gateway_url;
            final EditText smsGatewayUrl = (EditText) convertView.findViewById(R.id.smsGatewayUrl);
            smsGatewayUrl.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_url = s.toString();
                    setupData.sms_gateway_params.sms_gateway_url = s.toString();
                }
            });
            smsGatewayUrl.setText(setupData.sms_gateway_url);

            // username
            final View usernameLayout = convertView.findViewById(R.id.usernameLayout);
            final EditText username = (EditText) convertView.findViewById(R.id.username);
            username.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_params.username = s.toString();
                }
            });
            username.setText(setupData.sms_gateway_params.username);

            // password
            final View passwordLayout = convertView.findViewById(R.id.passwordLayout);
            final EditText password = (EditText) convertView.findViewById(R.id.password);
            password.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_params.password = s.toString();
                }
            });
            password.setText(setupData.sms_gateway_params.password);

            // auth_id
            final View authIdLayout = convertView.findViewById(R.id.authIdLayout);
            final EditText authId = (EditText) convertView.findViewById(R.id.authId);
            authId.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_params.auth_id = s.toString();
                }
            });
            authId.setText(setupData.sms_gateway_params.auth_id);

            // auth_token
            final View authTokenLayout = convertView.findViewById(R.id.authTokenLayout);
            final EditText authToken = (EditText) convertView.findViewById(R.id.authToken);
            authToken.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_params.auth_token = s.toString();
                }
            });
            authToken.setText(setupData.sms_gateway_params.auth_token);

            // senders_phone
            final View sendersPhoneLayout = convertView.findViewById(R.id.sendersPhoneLayout);
            final EditText sendersPhone = (EditText) convertView.findViewById(R.id.sendersPhone);
            sendersPhone.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    setupData.sms_gateway_params.senders_phone = s.toString();
                }
            });
            sendersPhone.setText(setupData.sms_gateway_params.senders_phone);

            // authentication spinner
            final View authenticationLayout = convertView.findViewById(R.id.authenticationLayout);
            final Spinner authentication = (Spinner) convertView.findViewById(R.id.authentication);
            ArrayAdapter<SmsAuthenticationSelect> authenticationAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.authentication_select);
            authentication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int authid = setupDataResult.authentication_select.get(position).id;
                    setupData.sms_gateway_params.authentication = String.valueOf(authid);
                    if(authid == 0) // no authentication
                    {
                        usernameLayout.setVisibility(View.GONE);
                        passwordLayout.setVisibility(View.GONE);
                    }
                    else if(!setupData.sms_gateway_params.request_method.equals("app"))
                    {
                        usernameLayout.setVisibility(View.VISIBLE);
                        passwordLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            authentication.setAdapter(authenticationAdapter);
            for(int i = 0; i < setupDataResult.authentication_select.size(); i++)
            {
                SmsAuthenticationSelect item = setupDataResult.authentication_select.get(i);
                if(String.valueOf(item.id).equals(setupData.sms_gateway_params.authentication))
                    authentication.setSelection(i);
            }

            // encoding spinner
            final View encodingLayout = convertView.findViewById(R.id.encodingLayout);
            final Spinner encoding = (Spinner) convertView.findViewById(R.id.encoding);
            ArrayAdapter<SmsGatewayEncoding> encodingAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.encoding_select);
            encoding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setupData.sms_gateway_params.encoding = String.valueOf(setupDataResult.encoding_select.get(position).id);
                }
            });
            encoding.setAdapter(encodingAdapter);
            for(int i = 0; i < setupDataResult.encoding_select.size(); i++)
            {
                SmsGatewayEncoding item = setupDataResult.encoding_select.get(i);
                if(item.id.equals(setupData.sms_gateway_params.encoding))
                    encoding.setSelection(i);
            }

            // enable/disable checkbox
            CheckBox enable = (CheckBox) convertView.findViewById(R.id.enableSmsGateway);
            enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    requestMethod.setEnabled(isChecked);
                    authentication.setEnabled(isChecked);
                    smsGatewayUrl.setEnabled(isChecked);
                }
            });
            enable.setChecked(setupData.sms_gateway == 1);
            requestMethod.setEnabled(setupData.sms_gateway == 1);
            authentication.setEnabled(setupData.sms_gateway == 1);
            smsGatewayUrl.setEnabled(setupData.sms_gateway == 1);

            // kept as a placeholder for future
            final View clearQueue = new View(context);// convertView.findViewById(R.id.clearQueue);

            // request method spinner

            final View urlLayout = convertView.findViewById(R.id.urlLayout);
            final TextView smsGatewayAppInfo = (TextView) convertView.findViewById(R.id.smsGatewayAppInfo);
            smsGatewayAppInfo.setText(String.format(context.getString(R.string.smsGatewayAppInfo), setupDataResult.sms_queue_count, setupData.sms_gateway_app_date));

            ArrayAdapter<SmsGatewayRequestMethod> requestMethodAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, setupDataResult.request_method_select);
            requestMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String method = setupDataResult.request_method_select.get(position).id;
                    if(method == null)
                        method = "get";

                    if(setupData.sms_gateway_params.authentication == null)
                        setupData.sms_gateway_params.authentication = "0";

                    authIdLayout.setVisibility(View.GONE);
                    authTokenLayout.setVisibility(View.GONE);
                    sendersPhoneLayout.setVisibility(View.GONE);
                    setupData.sms_gateway_params.request_method = method;
                    if(method.equals("get"))
                    {
                        authenticationLayout.setVisibility(View.VISIBLE);
                        clearQueue.setVisibility(View.GONE);
                        encodingLayout.setVisibility(View.GONE);
                        urlLayout.setVisibility(View.VISIBLE);
                        smsGatewayAppInfo.setVisibility(View.GONE);
                        if(setupData.sms_gateway_params.authentication.equals("0")) // no auth
                        {
                            usernameLayout.setVisibility(View.GONE);
                            passwordLayout.setVisibility(View.GONE);
                        }
                        else {
                            usernameLayout.setVisibility(View.VISIBLE);
                            passwordLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    else if(method.equals("post"))
                    {
                        authenticationLayout.setVisibility(View.VISIBLE);
                        clearQueue.setVisibility(View.GONE);
                        encodingLayout.setVisibility(View.VISIBLE);
                        urlLayout.setVisibility(View.VISIBLE);
                        smsGatewayAppInfo.setVisibility(View.GONE);
                        if(setupData.sms_gateway_params.authentication.equals("0")) // no auth
                        {
                            usernameLayout.setVisibility(View.GONE);
                            passwordLayout.setVisibility(View.GONE);
                        }
                        else {
                            usernameLayout.setVisibility(View.VISIBLE);
                            passwordLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    else if(method.equals("app"))
                    {
                        authenticationLayout.setVisibility(View.GONE);
                        clearQueue.setVisibility(View.VISIBLE);
                        encodingLayout.setVisibility(View.GONE);
                        urlLayout.setVisibility(View.GONE);
                        smsGatewayAppInfo.setVisibility(View.VISIBLE);
                        usernameLayout.setVisibility(View.GONE);
                        passwordLayout.setVisibility(View.GONE);
                    }
                    else if(method.equals("plivo"))
                    {
                        authenticationLayout.setVisibility(View.GONE);
                        clearQueue.setVisibility(View.GONE);
                        encodingLayout.setVisibility(View.GONE);
                        urlLayout.setVisibility(View.GONE);
                        smsGatewayAppInfo.setVisibility(View.GONE);
                        usernameLayout.setVisibility(View.GONE);
                        passwordLayout.setVisibility(View.GONE);
                        authIdLayout.setVisibility(View.VISIBLE);
                        authTokenLayout.setVisibility(View.VISIBLE);
                        sendersPhoneLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            requestMethod.setAdapter(requestMethodAdapter);
            for(int i = 0; i < setupDataResult.request_method_select.size(); i++)
            {
                SmsGatewayRequestMethod item = setupDataResult.request_method_select.get(i);
                if(item.id.equals(setupData.sms_gateway_params.request_method))
                    requestMethod.setSelection(i);
            }

            convertView.findViewById(R.id.sendTestSms).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SendTestSmsActivity.class);
                    intent.putExtra("params", new Gson().toJson(setupData.sms_gateway_params));
                    context.startActivity(intent);
                }
            });
        }
        else if(groupPosition == 5)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_smstemplates, null);
            ListView list = (ListView) convertView.findViewById(R.id.list);
            Button addNew = (Button) convertView.findViewById(R.id.addNew);

            list.setAdapter(new AwesomeAdapter<UserSmsTemplate>(context, smsTemplates)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null)
                        convertView = inflater.inflate(R.layout.adapter_simple_text_with_delete, null);

                    final UserSmsTemplate item = getItem(position);
                    TextView text = (TextView) convertView.findViewById(R.id.text);
                    text.setText(item.title);
                    TextView message = (TextView) convertView.findViewById(R.id.message);
                    message.setText(item.message);
                    View delete = convertView.findViewById(R.id.delete);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            API.getApiInterface(context).destroyUserSmsTemplate(api_key, Lang.getCurrentLanguage(), item.id, new Callback<ApiInterface.DestroyUserSmsTemplateResult>() {
                                @Override
                                public void success(ApiInterface.DestroyUserSmsTemplateResult destroyUserSmsTemplateResult, Response response) {
                                    smsTemplates.remove(item);
                                    SetupAdapter.this.notifyDataSetChanged();
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(context, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return convertView;
                }
            });

            addNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity)context).startActivityForResult(new Intent(context, InputSmsTemplateActivity.class), 1);
                }
            });
        }
        else if(groupPosition == 6)
        {
            convertView = inflater.inflate(R.layout.adapter_setup_gprstemplates, null);
            ListView list = (ListView) convertView.findViewById(R.id.list);
            Button addNew = (Button) convertView.findViewById(R.id.addNew);

            list.setAdapter(new AwesomeAdapter<UserGprsTemplate>(context, gprsTemplates)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null)
                        convertView = inflater.inflate(R.layout.adapter_simple_text_with_delete, null);

                    final UserGprsTemplate item = getItem(position);
                    TextView text = (TextView) convertView.findViewById(R.id.text);
                    text.setText(item.title);
                    TextView message = (TextView) convertView.findViewById(R.id.message);
                    message.setText(item.message);
                    View delete = convertView.findViewById(R.id.delete);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            API.getApiInterface(context).destroyUserGprsTemplate(api_key, Lang.getCurrentLanguage(), item.id, new Callback<ApiInterface.DestroyUserGprsTemplateResult>() {
                                @Override
                                public void success(ApiInterface.DestroyUserGprsTemplateResult destroyUserGprsTemplateResult, Response response) {
                                    gprsTemplates.remove(item);
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(context, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return convertView;
                }
            });

            addNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity)context).startActivityForResult(new Intent(context, InputGprsTemplateActivity.class), 2);
                }
            });
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void addSmsTemplate(UserSmsTemplate item)
    {
        smsTemplates.add(item);
        notifyDataSetChanged();
    }
    public void addGprsTemplate(UserGprsTemplate item)
    {
        gprsTemplates.add(item);
        notifyDataSetChanged();
    }
    public void addDriver(Driver item)
    {
        drivers.add(item);
        notifyDataSetChanged();
    }

    public void addCustomEvent(CustomEvent item) {
        events.add(item);
        notifyDataSetChanged();
    }

    public SetupData getSetupData()
    {
        return setupData;
    }
    public ArrayList<ObjectGroup> getObjectGroups()
    {
        return setupDataResult.groups;
    }
}
