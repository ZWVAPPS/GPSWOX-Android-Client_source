package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.SetupAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.CustomEvent;
import com.gpswox.android.models.Driver;
import com.gpswox.android.models.SetupData;
import com.gpswox.android.models.UserGprsTemplate;
import com.gpswox.android.models.UserSmsTemplate;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupActivity extends AppCompatActivity
{

    @Bind(R.id.back)
    View back;
    @Bind(R.id.expandable_list)
    ExpandableListView expandable_list;
    @Bind(R.id.saveChanges)
    View saveChanges;

    @Bind(R.id.content_layout)
    View content_layout;
    @Bind(R.id.loading_layout)
    View loading_layout;

    private SetupAdapter adapter;
    DataSaver dataSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        dataSaver = DataSaver.getInstance(this);

        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) dataSaver.load("api_key");
        // setup data
        API.getApiInterface(this).getSetupData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.SetupDataResult>()
        {
            @Override
            public void success(final ApiInterface.SetupDataResult setupDataResult, Response response)
            {

                // get drivers
                API.getApiInterface(SetupActivity.this).getUserDrivers(api_key, Lang.getCurrentLanguage(), 0, new Callback<ApiInterface.GetUserDriversResult>()
                {
                    @Override
                    public void success(final ApiInterface.GetUserDriversResult getUserDriversResult, Response response)
                    {

                        // get events
                        API.getApiInterface(SetupActivity.this).getCustomEvents(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetCustomEventsResult>()
                        {
                            @Override
                            public void success(final ApiInterface.GetCustomEventsResult getCustomEventsResult, Response response)
                            {
                                // get sms templates
                                API.getApiInterface(SetupActivity.this).getUserSmsTemplates(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetUserSmsTemplatesResult>()
                                {
                                    @Override
                                    public void success(final ApiInterface.GetUserSmsTemplatesResult getUserSmsTemplatesResult, Response response)
                                    {
                                        // get gprs templates
                                        API.getApiInterface(SetupActivity.this).getUserGprsTemplates(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetUserGprsTemplatesResult>()
                                        {
                                            @Override
                                            public void success(ApiInterface.GetUserGprsTemplatesResult getUserGprsTemplatesResult, Response response)
                                            {
                                                adapter = new SetupAdapter(SetupActivity.this, expandable_list, setupDataResult,
                                                        getUserDriversResult.items.drivers.data,
                                                        getCustomEventsResult.items.events.data,
                                                        getUserSmsTemplatesResult.items.user_sms_templates.data,
                                                        getUserGprsTemplatesResult.items.user_gprs_templates.data);
                                                expandable_list.setAdapter(adapter);
                                                Utils.setGroupClickListenerToNotify(expandable_list, adapter);

                                                loading_layout.setVisibility(View.GONE);
                                                content_layout.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void failure(RetrofitError retrofitError)
                                            {
                                                Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(RetrofitError retrofitError)
                                    {
                                        Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError retrofitError)
                    {
                        Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (adapter == null) return; // not loaded yet
                final SetupData data = adapter.getSetupData();
                if (data == null) return; // not loaded yet
                String groups_array = new Gson().toJson(adapter.getObjectGroups());
                API.getApiInterface(SetupActivity.this).saveEditedSetup(api_key, Lang.getCurrentLanguage(),
                        data.unit_of_distance,
                        data.unit_of_capacity,
                        data.unit_of_altitude,
                        data.timezone_id,
                        groups_array,
                        data.sms_gateway,
                        data.sms_gateway_params.request_method,
                        data.sms_gateway_params.encoding,
                        data.sms_gateway_params.authentication,
                        data.sms_gateway_params.username,
                        data.sms_gateway_params.password,
                        data.sms_gateway_url,
                        data.sms_gateway_params.auth_id,
                        data.sms_gateway_params.auth_token,
                        data.sms_gateway_params.senders_phone,
                        new Callback<ApiInterface.AddUserDriverResult>()
                        {
                            @Override
                            public void success(ApiInterface.AddUserDriverResult addUserDriverResult, Response response)
                            {
                                Toast.makeText(SetupActivity.this, "Successfully saved!", Toast.LENGTH_SHORT).show();
                                dataSaver.save("unit_of_distance", data.unit_of_distance);
                                dataSaver.save("unit_of_distance_hour", "mph");
                                if(data.unit_of_distance.equals("km"))
                                {
                                    dataSaver.save("unit_of_distance_hour", "kph");
                                }
                                dataSaver.save("unit_of_altitude", data.unit_of_altitude);
                                dataSaver.save("unit_of_capacity", data.unit_of_capacity);
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                Toast.makeText(SetupActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) // new sms template
        {
            UserSmsTemplate item = new Gson().fromJson(data.getStringExtra("item"), UserSmsTemplate.class);
            adapter.addSmsTemplate(item);
        } else if (requestCode == 2 && resultCode == RESULT_OK) // new gprs template
        {
            UserGprsTemplate item = new Gson().fromJson(data.getStringExtra("item"), UserGprsTemplate.class);
            adapter.addGprsTemplate(item);
        } else if (requestCode == 3 && resultCode == RESULT_OK) // new driver
        {
            Driver item = new Gson().fromJson(data.getStringExtra("item"), Driver.class);
            adapter.addDriver(item);
        } else if (requestCode == 4 && resultCode == RESULT_OK) // new custom template
        {
            CustomEvent item = new Gson().fromJson(data.getStringExtra("item"), CustomEvent.class);
            adapter.addCustomEvent(item);
        }
    }
}
