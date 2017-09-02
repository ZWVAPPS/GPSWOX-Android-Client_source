package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Alert;
import com.gpswox.android.utils.DataSaver;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AlertsActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.addAlert) View addAlert;
    @Bind(R.id.list) ListView list;
    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;

    private AwesomeAdapter<Alert> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        addAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AlertsActivity.this, InputAlertActivity.class));
            }
        });

        final String api_key = (String) DataSaver.getInstance(this).load("api_key");

        adapter = new AwesomeAdapter<Alert>(this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.adapter_alerts, null);

                final Alert item = getItem(position);
                final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                checkBox.setChecked(item.active == 1);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        API.getApiInterface(AlertsActivity.this).changeAlertActive(api_key, getResources().getString(R.string.lang), item.id, checkBox.isChecked(), new Callback<ApiInterface.ChangeAlertActiveResult>() {
                            @Override
                            public void success(ApiInterface.ChangeAlertActiveResult changeAlertActiveResult, Response response) {
                                item.active = item.active == 1 ? 0 : 1;
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Toast.makeText(AlertsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                TextView name = (TextView) convertView.findViewById(R.id.name);
                name.setText(item.name);

                convertView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(AlertsActivity.this, InputAlertActivity.class);
                        intent.putExtra("alert", new Gson().toJson(item));
                        startActivity(intent);
                    }
                });
                convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        API.getApiInterface(AlertsActivity.this).destroyAlert(api_key, getResources().getString(R.string.lang), item.id, new Callback<ApiInterface.DestroyAlertResult>() {
                            @Override
                            public void success(ApiInterface.DestroyAlertResult destroyAlertResult, Response response)
                            {
                                remove(item);
                                notifyDataSetChanged();
                                if(getCount() == 0) {
                                    content_layout.setVisibility(View.GONE);
                                    nodata_layout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                if (retrofitError.getResponse().getStatus() == 403) {
                                    Toast.makeText(AlertsActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(AlertsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh()
    {
        content_layout.setVisibility(View.GONE);
        nodata_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getAlerts((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetAlertsResult>() {
            @Override
            public void success(ApiInterface.GetAlertsResult getAlertsResult, Response response)
            {
                adapter.setArray(getAlertsResult.items.alerts);

                loading_layout.setVisibility(View.GONE);
                if(getAlertsResult.items.alerts.size() != 0)
                    content_layout.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(AlertsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
