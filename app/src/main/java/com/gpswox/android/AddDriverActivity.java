package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddDriverActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.name) EditText name;
    @Bind(R.id.device) Spinner devices;
    @Bind(R.id.rfid) EditText rfid;
    @Bind(R.id.phone) EditText phone;
    @Bind(R.id.email) EditText email;
    @Bind(R.id.description) EditText description;
    @Bind(R.id.addDriver) View addDriver;
    @Bind(R.id.contentLayout) View contentLayout;
    @Bind(R.id.loading_layout) View loading_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loading_layout.setVisibility(View.VISIBLE);
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                ArrayList<Device> totalDevices = new ArrayList<>();
                for(ApiInterface.GetDevicesItem item : getDevicesItems)
                    totalDevices.addAll(item.items);
                ArrayAdapter<Device> devicesAdapter = new ArrayAdapter<>(AddDriverActivity.this, R.layout.spinner_item, totalDevices);
                devices.setAdapter(devicesAdapter);
                loading_layout.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(AddDriverActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        addDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameStr = name.getText().toString();
                int device_id = ((Device) devices.getSelectedItem()).id;
                String rfidStr = rfid.getText().toString();
                String phoneStr = phone.getText().toString();
                String emailStr = email.getText().toString();
                String descriptionStr = description.getText().toString();

                if(!emailStr.equals("") && !Utils.isValidEmail(emailStr))
                    Toast.makeText(AddDriverActivity.this, R.string.invalidEmail, Toast.LENGTH_SHORT).show();
                else
                    API.getApiInterface(AddDriverActivity.this).addUserDriver(api_key, getResources().getString(R.string.lang), nameStr, device_id, rfidStr, phoneStr, emailStr, descriptionStr, new Callback<ApiInterface.AddUserDriverResult>() {
                        @Override
                        public void success(ApiInterface.AddUserDriverResult addUserDriverResult, Response response) {
                            Toast.makeText(AddDriverActivity.this, R.string.userDriverAdded, Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            data.putExtra("item", new Gson().toJson(addUserDriverResult.item));
                            setResult(RESULT_OK, data);
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(AddDriverActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

    }
}
