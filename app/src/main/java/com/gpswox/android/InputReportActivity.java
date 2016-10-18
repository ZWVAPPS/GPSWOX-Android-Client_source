package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpswox.android.adapters.EditReportAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Report;
import com.gpswox.android.models.ReportType;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class InputReportActivity extends AppCompatActivity {
    @Bind(R.id.back) View back;
    @Bind(R.id.actionbar_title) TextView actionbar_title;
    @Bind(R.id.save_report) View save_report;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;
    @Bind(R.id.loading_layout) View loading_layout;

    private Report report;
    private ArrayList<ReportType> types;
    private EditReportAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);
        ButterKnife.bind(this);

        report = new Report();
        if(getIntent().hasExtra("report")) {
            report = new Gson().fromJson(getIntent().getStringExtra("report"), Report.class);
            actionbar_title.setText(R.string.editReport);
        }

        types = new Gson().fromJson(getIntent().getStringExtra("types"), new TypeToken<List<ReportType>>(){}.getType());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getDataForReports((String) DataSaver.getInstance(this).load("api_key"), Lang.getCurrentLanguage(), new Callback<ApiInterface.GetReportDataResult>() {
            @Override
            public void success(ApiInterface.GetReportDataResult getReportDataResult, Response response) {
                adapter = new EditReportAdapter(InputReportActivity.this, report, types, getReportDataResult);
                expandable_list.setAdapter(adapter);
                Utils.setGroupClickListenerToNotify(expandable_list, adapter);

                loading_layout.setVisibility(View.GONE);
                expandable_list.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(InputReportActivity.this, getString(R.string.errorHappened), Toast.LENGTH_SHORT).show();
            }
        });

        save_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Report report = adapter.getReport();
                if(report.email.equals("") || !Utils.isValidEmail(report.email)) {
                    Toast.makeText(InputReportActivity.this, R.string.invalidEmail, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(report.title.equals(""))
                {
                    Toast.makeText(InputReportActivity.this, R.string.titleFieldRequired, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(report.devices.size() <= 0)
                {
                    Toast.makeText(InputReportActivity.this, R.string.mustSelectMinOneDevice, Toast.LENGTH_SHORT).show();
                    return;
                }
                // type 7 susijes su geofences
                else if(report.type == 7 && report.geofences.size() <= 0)
                {
                    Toast.makeText(InputReportActivity.this, R.string.mustSelectMinOneGeofence, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(getIntent().hasExtra("report")) // was editing
                {
                    String devices_array = new Gson().toJson(report.devices);
                    String geofences_array = new Gson().toJson(report.geofences);
                    API.getApiInterface(InputReportActivity.this).saveEditedReport((String) DataSaver.getInstance(InputReportActivity.this).load("api_key"), Lang.getCurrentLanguage(),
                            report.id,
                            report.title, report.type, report.format, report.stops, report.speed_limit, devices_array, geofences_array, report.daily, report.weekly, report.email, new Callback<ApiInterface.SaveEditedReportResult>() {
                                @Override
                                public void success(ApiInterface.SaveEditedReportResult saveEditedReportResult, Response response) {
                                    Toast.makeText(InputReportActivity.this, R.string.reportSaved, Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(InputReportActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else // add new
                {
                    String devices_array = new Gson().toJson(report.devices);
                    String geofences_array = new Gson().toJson(report.geofences);
                    API.getApiInterface(InputReportActivity.this).addNewReport((String) DataSaver.getInstance(InputReportActivity.this).load("api_key"), Lang.getCurrentLanguage(),
                            report.title, report.type, report.format, report.stops, report.speed_limit, devices_array, geofences_array, report.daily, report.weekly, report.email, new Callback<ApiInterface.AddNewReportResult>() {
                                @Override
                                public void success(ApiInterface.AddNewReportResult addNewReportResult, Response response) {
                                    Toast.makeText(InputReportActivity.this, R.string.newReportAdded, Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    Toast.makeText(InputReportActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}
