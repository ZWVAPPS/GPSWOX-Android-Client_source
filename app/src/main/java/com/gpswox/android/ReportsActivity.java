package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Report;
import com.gpswox.android.models.ReportType;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReportsActivity extends AppCompatActivity
{

    @Bind(R.id.back) View back;
    @Bind(R.id.listview) ListView listview;
    @Bind(R.id.add_report) View addReport;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;

    private AwesomeAdapter<Report> adapter;
    private ArrayList<ReportType> types;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        ButterKnife.bind(this);

        addReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (types == null) return;
                Intent intent = new Intent(ReportsActivity.this, InputReportActivity.class);
                intent.putExtra("types", new Gson().toJson(types));
                startActivity(intent);
            }
        });

        adapter = new AwesomeAdapter<Report>(this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_report, null);

                final Report item = getItem(position);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(item.title);
                TextView type = (TextView) convertView.findViewById(R.id.type);

                for(ReportType reportType : types)
                    if(reportType.id == item.type)
                        type.setText(reportType.title);

                TextView format = (TextView) convertView.findViewById(R.id.formatValue);
                format.setText(item.format.toUpperCase());
                TextView devices = (TextView) convertView.findViewById(R.id.devicesValue);
                devices.setText(String.valueOf(item.devices.size()));
                TextView geofences = (TextView) convertView.findViewById(R.id.geofencesValue);
                geofences.setText(String.valueOf(item.geofences.size()));
                TextView schedule = (TextView) convertView.findViewById(R.id.scheduleValue);
                if(item.daily > 0)
                    schedule.setText(R.string.daily);
                if(item.weekly > 0)
                    schedule.setText(R.string.weekly);
                if(item.daily > 0 && item.weekly > 0)
                    schedule.setText(getString(R.string.daily) + "/" + getString(R.string.weekly));
                if(item.daily <= 0 && item.weekly <= 0)
                    schedule.setText(R.string.no);

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
                    }
                });

                convertView.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), InputReportActivity.class);
                        intent.putExtra("report", new Gson().toJson(item));
                        intent.putExtra("types", new Gson().toJson(types));
                        getContext().startActivity(intent);
                    }
                });

                convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        API.getApiInterface(getContext()).deleteReport((String) DataSaver.getInstance(getContext()).load("api_key"), item.id, Lang.getCurrentLanguage(), new Callback<ApiInterface.DeleteReportResult>() {
                            @Override
                            public void success(ApiInterface.DeleteReportResult result, Response response)
                            {
                                remove(item);
                                if(getCount() == 0) {
                                    listview.setVisibility(View.GONE);
                                    nodata_layout.setVisibility(View.VISIBLE);
                                }
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

        refresh();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh()
    {
        listview.setVisibility(View.GONE);
        nodata_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getReports((String) DataSaver.getInstance(this).load("api_key"), Lang.getCurrentLanguage(), new Callback<ApiInterface.GetReportsResult>() {
            @Override
            public void success(ApiInterface.GetReportsResult getReportsResult, Response response) {
                types = getReportsResult.items.types;
                listview.setAdapter(adapter);
                adapter.setArray(getReportsResult.items.reports.data);

                loading_layout.setVisibility(View.GONE);
                if(getReportsResult.items.reports.data.size() != 0)
                    listview.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("ReportsActivity", "ERROR GETTING REPORTS: " + retrofitError.getMessage());
                Toast.makeText(ReportsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
