package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.SmsGatewayParams;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SendTestSmsActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.phoneNumber) EditText phoneNumber;
    @Bind(R.id.message) EditText message;
    @Bind(R.id.send) View send;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_test_sms);
        ButterKnife.bind(this);

        final SmsGatewayParams params = new Gson().fromJson(getIntent().getStringExtra("params"), SmsGatewayParams.class);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile_phone = phoneNumber.getText().toString();
                String text = message.getText().toString();
                API.getApiInterface(SendTestSmsActivity.this).sendTestSms((String) DataSaver.getInstance(SendTestSmsActivity.this).load("api_key"), getResources().getString(R.string.lang),
                        params.request_method, params.authentication, params.username, params.password, params.encoding, params.auth_id, params.auth_token, params.senders_phone, params.sms_gateway_url, mobile_phone, text, new Callback<ApiInterface.SendTestSmsResult>() {
                            @Override
                            public void success(ApiInterface.SendTestSmsResult sendTestSmsResult, Response response) {
                                Toast.makeText(SendTestSmsActivity.this, R.string.testSmsSent, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                Toast.makeText(SendTestSmsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
