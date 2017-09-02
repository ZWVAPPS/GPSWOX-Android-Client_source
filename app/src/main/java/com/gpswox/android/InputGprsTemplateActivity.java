package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class InputGprsTemplateActivity extends AppCompatActivity {

    @Bind(R.id.back) View back;
    @Bind(R.id.title) EditText title;
    @Bind(R.id.message) EditText message;
    @Bind(R.id.addNew)
    View addNew;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_gprs_template);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleStr = title.getText().toString();
                String messageStr = message.getText().toString();
                if(titleStr.equals(""))
                    Toast.makeText(InputGprsTemplateActivity.this, R.string.titleFieldRequired, Toast.LENGTH_SHORT).show();
                else if(messageStr.equals(""))
                    Toast.makeText(InputGprsTemplateActivity.this, R.string.messageFieldRequired, Toast.LENGTH_SHORT).show();
                else
                    API.getApiInterface(InputGprsTemplateActivity.this).addUserGprsTemplate((String) DataSaver.getInstance(InputGprsTemplateActivity.this).load("api_key"), getResources().getString(R.string.lang), titleStr, messageStr, new Callback<ApiInterface.AddUserGprsTemplateResult>() {
                        @Override
                        public void success(ApiInterface.AddUserGprsTemplateResult addUserGprsTemplateResult, Response response) {
                            Intent data = new Intent();
                            data.putExtra("item", new Gson().toJson(addUserGprsTemplateResult.item));
                            setResult(RESULT_OK, data);
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(InputGprsTemplateActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }
}
