package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.POIMarker;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class POIActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.addMarker) View addMarker;
    @Bind(R.id.list) ListView list;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;

    private AwesomeAdapter<POIMarker> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        adapter = new AwesomeAdapter<POIMarker>(POIActivity.this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_poimarkers, null);
                final POIMarker item = getItem(position);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(item.name);
                TextView description = (TextView) convertView.findViewById(R.id.description);
                description.setText(item.description);
                View delete = convertView.findViewById(R.id.delete);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        API.getApiInterface(POIActivity.this).destroyPOIMarker(api_key, Lang.getCurrentLanguage(), item.id, new Callback<ApiInterface.DestroyPOIMarkerResult>() {
                            @Override
                            public void success(ApiInterface.DestroyPOIMarkerResult destroyPOIMarkerResult, Response response) {
                                adapter.remove(item);
                                notifyDataSetChanged();
                                if(getCount() == 0) {
                                    content_layout.setVisibility(View.GONE);
                                    nodata_layout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                if (retrofitError.getResponse().getStatus() == 403) {
                                    Toast.makeText(POIActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(POIActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        list.setAdapter(adapter);

        refresh();

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(POIActivity.this, AddPOIMarkerActivity.class));
            }
        });
    }

    private void refresh()
    {
        content_layout.setVisibility(View.GONE);
        nodata_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).loadPOIMarkers(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.LoadPOIMarkersResult>() {
            @Override
            public void success(ApiInterface.LoadPOIMarkersResult loadPOIMarkersResult, Response response)
            {
                adapter.setArray(loadPOIMarkersResult.items.mapIcons);

                loading_layout.setVisibility(View.GONE);
                if(loadPOIMarkersResult.items.mapIcons.size() != 0)
                    content_layout.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(POIActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
