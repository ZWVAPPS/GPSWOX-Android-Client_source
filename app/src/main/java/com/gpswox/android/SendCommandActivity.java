package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.SendCommandCommand;
import com.gpswox.android.models.SendCommandDevice;
import com.gpswox.android.models.SendCommandTemplate;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendCommandActivity extends AppCompatActivity
{
    @Bind(R.id.back)
    View back;
    @Bind(R.id.expandable_list)
    ExpandableListView expandable_list;
    @Bind(R.id.send)
    View send;

    @Bind(R.id.loading_layout)
    View loading_layout;
    @Bind(R.id.content_layout)
    View content_layout;
    ApiInterface.SendCommandData data;

    final String TAG = getClass().getSimpleName();
    SendCommandDevice gprs_selectedDevice;
    SendCommandCommand gprs_selectedCommand;

    int sms_lastChecked_template = 0;
    ArrayList<Integer> sms_devices_ids = new ArrayList<>();
    String sms_message;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        final BaseExpandableListAdapter adapter = new BaseExpandableListAdapter()
        {
            @Override
            public int getGroupCount()
            {
                return 2;
            }

            @Override
            public int getChildrenCount(int groupPosition)
            {
                return 1;
            }

            @Override
            public Object getGroup(int groupPosition)
            {
                return null;
            }

            @Override
            public Object getChild(int groupPosition, int childPosition)
            {
                return null;
            }

            @Override
            public long getGroupId(int groupPosition)
            {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition)
            {
                return 0;
            }

            @Override
            public boolean hasStableIds()
            {
                return false;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
            {
                if(convertView != null)
                {
                    return convertView;
                }
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_expandable_parent, null);
                String titleText = "";
                switch (groupPosition)
                {
                    case 0:
                        titleText = getString(R.string.gprs);
                        break;
                    case 1:
                        titleText = getString(R.string.sms);
                        break;
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(titleText);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
            {
                if(convertView != null)
                {
                    convertView.findViewById(R.id.message).requestFocus();
                    return convertView;
                }
                if (groupPosition == 0)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_sendcommand_gprs, parent);
                    Spinner device = (Spinner) convertView.findViewById(R.id.device);
                    final ArrayAdapter<SendCommandDevice> deviceAdapter = new ArrayAdapter<>(SendCommandActivity.this, R.layout.spinner_item, data.devices_gprs);
                    device.setAdapter(deviceAdapter);
                    device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onNothingSelected(AdapterView<?> parent)
                        {
                        }

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            gprs_selectedDevice = deviceAdapter.getItem(position);
                        }
                    });
                    for (int i = 0; i < data.devices_gprs.size(); i++)
                    {
                        SendCommandDevice item = data.devices_gprs.get(i);
                        if (item.equals(gprs_selectedDevice))
                            device.setSelection(i);
                    }

                    Spinner type = (Spinner) convertView.findViewById(R.id.type);
                    final ArrayAdapter<SendCommandCommand> typesAdapter = new ArrayAdapter<>(SendCommandActivity.this, R.layout.spinner_item, data.commands);
                    type.setAdapter(typesAdapter);
                    type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onNothingSelected(AdapterView<?> parent)
                        {
                        }

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            gprs_selectedCommand = typesAdapter.getItem(position);
                        }
                    });
                    for (int i = 0; i < data.commands.size(); i++)
                    {
                        SendCommandCommand item = data.commands.get(i);
                        if (gprs_selectedCommand != null && gprs_selectedCommand.id.equals(item.id))
                            type.setSelection(i);
                    }
                } else if (groupPosition == 1)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_sendcommand_sms, null);
                    ListView devices_list = (ListView) convertView.findViewById(R.id.devices_list);
                    devices_list.setAdapter(new AwesomeAdapter<SendCommandDevice>(SendCommandActivity.this, data.devices_sms)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent)
                        {
                            convertView = getLayoutInflater().inflate(R.layout.adapter_sendcommand_devices, null);
                            final SendCommandDevice item = getItem(position);
                            CheckBox device = (CheckBox) convertView.findViewById(R.id.device);
                            device.setText(item.value);
                            device.setChecked(false);
                            device.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                            {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                {
                                    if (isChecked)
                                    {
                                        if (!sms_devices_ids.contains(item.id))
                                            sms_devices_ids.add(item.id);
                                    } else
                                    {
                                        for (int i = 0; i < sms_devices_ids.size(); i++)
                                        {
                                            if (sms_devices_ids.get(i) == item.id)
                                                sms_devices_ids.remove(i);
                                        }
                                    }
                                }
                            });
                            for (Integer id : sms_devices_ids)
                                if (id == item.id)
                                    device.setChecked(true);
                            return convertView;
                        }
                    });
                    final EditText message = (EditText) convertView.findViewById(R.id.message);
                    Spinner smsTemplate = (Spinner) convertView.findViewById(R.id.smsTemplate);
                    final ArrayAdapter<SendCommandTemplate> smsTemplateAdapter = new ArrayAdapter<>(SendCommandActivity.this, R.layout.spinner_item, data.sms_templates);
                    smsTemplate.setAdapter(smsTemplateAdapter);
                    smsTemplate.setSelection(sms_lastChecked_template);
                    smsTemplate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        boolean first = true;

                        @Override
                        public void onNothingSelected(AdapterView<?> parent)
                        {
                        }

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            if (first) // dirty fix... dont call first time
                            {
                                first = false;
                                return;
                            }
                            message.setText(smsTemplateAdapter.getItem(position).message);
                            sms_lastChecked_template = position;
                            Log.d(TAG, "onItemSelected: called");
                        }
                    });

                    message.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after)
                        {
                        }

                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            sms_message = s.toString();
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count)
                        {

                        }
                    });
                    /*if (sms_message != null)
                        message.setText(sms_message);*/
                }
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition)
            {
                return false;
            }
        };


        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getSendCommandData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.SendCommandData>()
        {
            @Override
            public void success(ApiInterface.SendCommandData sendCommandData, Response response)
            {
                data = sendCommandData;
                expandable_list.setAdapter(adapter);

                content_layout.setVisibility(View.VISIBLE);
                loading_layout.setVisibility(View.GONE);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                if (retrofitError.getResponse().getStatus() == 403)
                {
                    Toast.makeText(SendCommandActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(SendCommandActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
            }
        });

        expandable_list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener()
        {
            @Override
            public void onGroupExpand(int groupPosition)
            {
                if (groupPosition == 1)
                    expandable_list.collapseGroup(0);
                else
                    expandable_list.collapseGroup(1);
            }
        });

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (expandable_list.isGroupExpanded(0)) // gprs
                {
                    API.getApiInterface(SendCommandActivity.this).sendGprsCommand(api_key, Lang.getCurrentLanguage(), 1, "minute", gprs_selectedDevice.id, gprs_selectedCommand.id, new Callback<ApiInterface.SendGprsCommandResult>()
                    {
                        @Override
                        public void success(ApiInterface.SendGprsCommandResult sendGprsCommandResult, Response response)
                        {
                            Toast.makeText(SendCommandActivity.this, R.string.gprsCommandSent, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError)
                        {
                            Toast.makeText(SendCommandActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else // sms
                {
                    String devices_array = TextUtils.join(",", sms_devices_ids);

                    System.out.println("devices_array : " + devices_array);
                    API.getApiInterface(SendCommandActivity.this).sendSmsCommand(api_key, Lang.getCurrentLanguage(), 1, "minute", sms_message, devices_array, new Callback<ApiInterface.SendSmsCommandResult>()
                    {
                        @Override
                        public void success(ApiInterface.SendSmsCommandResult sendSmsCommandResult, Response response)
                        {
                            Toast.makeText(SendCommandActivity.this, R.string.smsCommandSent, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError)
                        {
                            Toast.makeText(SendCommandActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
