package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.gpswox.android.adapters.ObjectInfoAdapter;
import com.gpswox.android.models.Device;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ObjectInfoActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.actionbar_title) TextView actionbar_title;
    @Bind(R.id.expandable_list) ExpandableListView expandable_list;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_info);
        ButterKnife.bind(this);

        Device device = new Gson().fromJson(getIntent().getStringExtra("device"), Device.class);
        actionbar_title.setText(device.name);

        expandable_list.setAdapter(new ObjectInfoAdapter(this, device));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
