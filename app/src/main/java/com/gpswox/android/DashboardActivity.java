package com.gpswox.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DashboardActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private class DashboardItem
    {
        public int titleResId;
        public int resId;
        public Class activityClass;

        public DashboardItem(int titleResId, int resId, Class activityClass)
        {
            this.titleResId = titleResId;
            this.resId = resId;
            this.activityClass = activityClass;
        }
    }

    @Bind(R.id.logout)
    View logout;
    @Bind(R.id.gridview)
    GridView gridview;
    @Bind(R.id.myAccount)
    View myAccount;
    @Bind(R.id.support)
    View support;

    private AwesomeAdapter<DashboardItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        adapter = new AwesomeAdapter<DashboardItem>(this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_dashboard, null);

                DashboardItem item = getItem(position);
                ImageView image = (ImageView) convertView.findViewById(R.id.image);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                image.setImageResource(item.resId);
                title.setText(getString(item.titleResId));

                // extremely dirty hack - divider'iams reik background color nustatyt, o gridview'as ne stretch'ina visų itemų iki dugno
                // stretchinam priverstinai

                /*int totalHeight = getWindow().getDecorView().getHeight();
                totalHeight -= Utils.dpToPx(DashboardActivity.this, 48); // actionbaro height atimam
                totalHeight -= Utils.dpToPx(DashboardActivity.this, 50); // footerio height atimam
                totalHeight = totalHeight * 55 / 100; // weight...*/
                convertView.setMinimumHeight(gridview.getHeight() / 2);

                return convertView;
            }
        };
        final ArrayList<DashboardItem> array = new ArrayList<>();
        array.add(new DashboardItem(R.string.map, R.drawable.icon_map, MapActivity.class));
        array.add(new DashboardItem(R.string.objects, R.drawable.icon_objects, ObjectsActivity.class));
        array.add(new DashboardItem(R.string.events, R.drawable.icon_events, EventsActivity.class));
        array.add(new DashboardItem(R.string.history, R.drawable.icon_history, HistoryActivity.class));
        array.add(new DashboardItem(R.string.tools, R.drawable.icon_tools, ToolsActivity.class));
        array.add(new DashboardItem(R.string.setup, R.drawable.icon_setup, SetupActivity.class));
        adapter.setArray(array);

        gridview.setAdapter(adapter);
        //gridview.setEnabled(false);

        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DataSaver.getInstance(DashboardActivity.this).save("api_key", null);
                finish();
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                try
                {
                    startActivity(new Intent(DashboardActivity.this, adapter.getItem(position).activityClass));
                } catch (Exception e)
                {
                    Toast.makeText(DashboardActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(DashboardActivity.this, MyAccountActivity.class));
            }
        });

        support.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getResources().getString(R.string.support_email), null));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sendEmail)));
            }
        });

        // set units on the first load only
        if (DataSaver.getInstance(DashboardActivity.this).load("unit_of_distance") == null)
        {
            final String api_key = (String) DataSaver.getInstance(this).load("api_key");
            API.getApiInterface(this).getDevices(api_key, Lang.getCurrentLanguage(), new Callback<ArrayList<ApiInterface.GetDevicesItem>>()
            {
                @Override
                public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
                {
                    Log.d(TAG, "success: loaded devices array");
                    final ArrayList<Device> allDevices = new ArrayList<>();
                    if (getDevicesItems != null)
                        for (ApiInterface.GetDevicesItem item : getDevicesItems)
                            allDevices.addAll(item.items);
                    if (allDevices.size() > 0)
                    {
                        Device device = allDevices.get(0);
                        DataSaver.getInstance(DashboardActivity.this).save("unit_of_distance_hour", device.distance_unit_hour);
                        DataSaver.getInstance(DashboardActivity.this).save("unit_of_distance", device.unit_of_distance);
                        DataSaver.getInstance(DashboardActivity.this).save("unit_of_capacity", device.unit_of_capacity);
                        DataSaver.getInstance(DashboardActivity.this).save("unit_of_altitude", device.unit_of_altitude);
                    }
                    Log.d(TAG, "success: loaded devices array");
                }

                @Override
                public void failure(RetrofitError retrofitError)
                {
                    Log.d(TAG, "failure: loaded devices array");
                }
            });
        }
    }
}
