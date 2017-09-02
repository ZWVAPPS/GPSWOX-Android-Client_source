package com.gpswox.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.gpswox.android.adapters.MyAccountAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyAccountActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;

    @Bind(R.id.loading_layout) View loading_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        ButterKnife.bind(this);


        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getMyAccountData((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetMyAccountDataResult>() {
            @Override
            public void success(ApiInterface.GetMyAccountDataResult dataResult, Response response)
            {
                MyAccountAdapter adapter = new MyAccountAdapter(MyAccountActivity.this, dataResult);
                expandable_list.setAdapter(adapter);
                Utils.setGroupClickListenerToNotify(expandable_list, adapter);

                loading_layout.setVisibility(View.GONE);
                expandable_list.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MyAccountActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.getSupport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",getResources().getString(R.string.support_email), null));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sendEmail)));
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
