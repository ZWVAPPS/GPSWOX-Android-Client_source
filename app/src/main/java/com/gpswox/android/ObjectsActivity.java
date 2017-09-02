package com.gpswox.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.gpswox.android.adapters.ObjectsAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ObjectsActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;
    @Bind(R.id.add_device) View add_device;

    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;
    @Bind(R.id.search) View search;

    private String searchtext;
    private ObjectsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objects);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        refresh();

        add_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ObjectsActivity.this, AddDeviceActivity.class));
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(ObjectsActivity.this);
                input.setSingleLine(true);
                if(searchtext != null) {
                    input.setText(searchtext);
                    input.setSelection(searchtext.length());
                }
                new AlertDialog.Builder(ObjectsActivity.this)
                        .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                searchtext = input.getText().toString();
                                adapter.getFilter().filter(searchtext);
                            }
                        })
                        .setView(input, Utils.dpToPx(ObjectsActivity.this, 10), 0, Utils.dpToPx(ObjectsActivity.this, 10), 0)
                        .setTitle(R.string.inputSearch)
                        .setNegativeButton(R.string.cancel, null)
                        .show();
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
        content_layout.setVisibility(View.GONE);
        nodata_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.VISIBLE);
        String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response) {
                adapter = new ObjectsAdapter(ObjectsActivity.this, getDevicesItems);
                expandable_list.setAdapter(adapter);
                Utils.setGroupClickListenerToNotify(expandable_list, adapter);

                loading_layout.setVisibility(View.GONE);

                if(getDevicesItems.size() != 0)
                    content_layout.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("ObjectsActivity", "failur: " + retrofitError.getMessage());
                Toast.makeText(ObjectsActivity.this, getString(R.string.errorHappened), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
