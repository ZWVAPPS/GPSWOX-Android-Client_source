package com.gpswox.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;
import java.util.StringTokenizer;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "EditObjectActivity";
    @Bind(R.id.username) EditText username;
    @Bind(R.id.password) EditText password;
    @Bind(R.id.spinner) Spinner serversSpinner;
    @Bind(R.id.signin) Button signin;
    @Bind(R.id.register) Button register;

    private String serverAddress;
    private String UrlPrefix;
    private String customServerAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(DataSaver.getInstance(this).load("api_key") != null)
        {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        String ip = getResources().getString(R.string.ip);
        String httpsOn = getResources().getString(R.string.https_on);

        if (httpsOn.equals("on")) {
            UrlPrefix = "https://";
        }
        else {
            UrlPrefix = "http://";
        }


        final ArrayList<String> serversArray = new ArrayList<>();
        final ArrayList<String> serversShowingArray = new ArrayList<>();;

        if (ip.isEmpty()) {
            serversArray.add("http://www.eu.gpswoxtracker.com/");
            serversArray.add("http://www.us.gpswoxtracker.com/");
            serversArray.add("http://www.asia.gpswoxtracker.com/");
            serversArray.add("custom");

            serversShowingArray.add("Europe");
            serversShowingArray.add("USA");
            serversShowingArray.add("Asia");
            serversShowingArray.add("Custom");
            serverAddress = "http://www.gpswox.com/";
        }
        else {
            String url = UrlPrefix + ip + "/";
            serversArray.add(url);

            serversShowingArray.add(ip);

            serverAddress = ip;
        }

if(serversArray.size() == 1)
{
    serversSpinner.setVisibility(View.INVISIBLE);
    ImageView spinnerArrow = (ImageView) findViewById(R.id.spinner_arrow);
    spinnerArrow.setVisibility(View.INVISIBLE);
}
        final ArrayAdapter<String> serversAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, serversShowingArray);
        serversSpinner.setAdapter(serversAdapter);

        serversSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) // custom
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                    final EditText edittext = new EditText(MainActivity.this);
                    alert.setMessage(R.string.inputCustom);
                    alert.setTitle(R.string.custom);

                    alert.setView(edittext);

                    alert.setPositiveButton(R.string.continuestr, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            customServerAddress = edittext.getText().toString();
                            String serverURL = customServerAddress;
                        }
                    });
                    alert.show();

                }
                else
                    customServerAddress = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String server_base = customServerAddress != null ? customServerAddress : serverAddress;
        if(!server_base.startsWith("http://") && !server_base.startsWith("https://"))
            server_base = UrlPrefix + server_base;
        if(!server_base.endsWith("/"))
            server_base += "/";
        DataSaver.getInstance(MainActivity.this).save("server_base", server_base);
        DataSaver.getInstance(MainActivity.this).save("server", server_base + "api/");

        if (ip.isEmpty()) {
            enableRegistration();
        }
        else {
            API.getApiInterface(MainActivity.this).registrationStatus("en", new Callback<ApiInterface.RegistrationStatusResult>()
            {
                @Override
                public void success(ApiInterface.RegistrationStatusResult result, Response response)
                {
                    if (result.status == 1) {
                        enableRegistration();
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    //Log.e(TAG, "failure: retrofitError" + retrofitError.getMessage());
                    //Toast.makeText(MainActivity.this, getString(R.string.errorHappened), Toast.LENGTH_SHORT).show();
                }
            });
        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customServerAddress != null)
                {
                    String server_base = customServerAddress;
                    if(!server_base.startsWith("http://") && !server_base.startsWith("https://"))
                        server_base = UrlPrefix + server_base;
                    if(!server_base.endsWith("/"))
                        server_base += "/";
                    DataSaver.getInstance(MainActivity.this).save("server_base", server_base);
                    DataSaver.getInstance(MainActivity.this).save("server", server_base + "api/");
                }
                API.getApiInterface(MainActivity.this).login(username.getText().toString(), password.getText().toString(), new Callback<ApiInterface.LoginResult>()
                {
                    @Override
                    public void success(ApiInterface.LoginResult loginResult, Response response)
                    {
                        DataSaver.getInstance(MainActivity.this).save("api_key", loginResult.user_api_hash);
                        Log.d("MainActivity", "api_key: " + loginResult.user_api_hash);

                        if(customServerAddress == null)
                        {
                            String url = serversArray.get(serversSpinner.getSelectedItemPosition());
                            DataSaver.getInstance(MainActivity.this).save("server_base", url);
                            DataSaver.getInstance(MainActivity.this).save("server", url + "api/");
                        }

                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError)
                    {
                        Toast.makeText(MainActivity.this, getString(R.string.wrongLogin), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void enableRegistration()
    {
        register.setBackgroundResource(R.drawable.button_blue);
        register.setEnabled(true);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = getResources().getString(R.string.ip);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ip.isEmpty() ? "http://www.gpswox.com/en/register" : "http://" + ip + "/registration/create"));
                startActivity(browserIntent);
            }
        });
    }
}
