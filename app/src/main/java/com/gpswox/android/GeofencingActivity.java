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

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Geofence;
import com.gpswox.android.utils.DataSaver;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GeofencingActivity extends AppCompatActivity
{
    @Bind(R.id.back)
    View back;
    @Bind(R.id.addGeofence)
    View addGeofence;
    @Bind(R.id.list)
    ListView list;
    AwesomeAdapter<Geofence> adapter;

    @Bind(R.id.content_layout)
    View content_layout;
    @Bind(R.id.loading_layout)
    View loading_layout;
    @Bind(R.id.nodata_layout)
    View nodata_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofencing);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
        refresh();

        final String api_key = (String) DataSaver.getInstance(this).load("api_key");

        adapter = new AwesomeAdapter<Geofence>(this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                convertView = getLayoutInflater().inflate(R.layout.adapter_geofences, null);
                final Geofence item = getItem(position);
                CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                View delete = convertView.findViewById(R.id.delete);
                checkbox.setChecked(item.active == 1);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        API.getApiInterface(GeofencingActivity.this).setGeofenceActive(api_key, getResources().getString(R.string.lang), item.id, isChecked, new Callback<ApiInterface.SetGeofenceActiveResult>()
                        {
                            @Override
                            public void success(ApiInterface.SetGeofenceActiveResult setGeofenceActiveResult, Response response)
                            {
                                item.active = item.active == 1 ? 0 : 1;
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                Toast.makeText(GeofencingActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                name.setText(item.name);
                delete.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        API.getApiInterface(GeofencingActivity.this).destroyGeofence(api_key, getResources().getString(R.string.lang), item.id, new Callback<ApiInterface.DestroyGeofenceResult>()
                        {
                            @Override
                            public void success(ApiInterface.DestroyGeofenceResult destroyGeofenceResult, Response response)
                            {
                                remove(item);
                                notifyDataSetChanged();
                                if (getCount() == 0)
                                {
                                    content_layout.setVisibility(View.GONE);
                                    nodata_layout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                if (retrofitError.getResponse().getStatus() == 403)
                                {
                                    Toast.makeText(GeofencingActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                                } else
                                {
                                    Toast.makeText(GeofencingActivity.this, retrofitError.getResponse().getStatus(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        list.setAdapter(adapter);

        addGeofence.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(GeofencingActivity.this, AddGeofenceActivity.class));
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        refresh();
    }

    private void refresh()
    {
        content_layout.setVisibility(View.GONE);
        nodata_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getGeofences((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetGeofencesResult>()
        {
            @Override
            public void success(ApiInterface.GetGeofencesResult getGeofencesResult, Response response)
            {
                loading_layout.setVisibility(View.GONE);
                if (getGeofencesResult.items.geofences.size() != 0)
                    content_layout.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
                adapter.setArray(getGeofencesResult.items.geofences);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                if (retrofitError.getResponse().getStatus() == 403)
                {
                    Toast.makeText(GeofencingActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(GeofencingActivity.this, retrofitError.getResponse().getStatus(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
