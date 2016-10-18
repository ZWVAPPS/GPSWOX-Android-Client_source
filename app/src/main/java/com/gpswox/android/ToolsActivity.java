package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gpswox.android.adapters.AwesomeAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ToolsActivity extends AppCompatActivity {

    @Bind(R.id.back) View back;
    @Bind(R.id.listview) ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);
        ButterKnife.bind(this);

        AwesomeAdapter<Pair<String, Class>> adapter = new AwesomeAdapter<Pair<String, Class>>(this) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_tools, null);
                Pair item = getItem(position);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(String.valueOf(item.first));
                return convertView;
            }
        };
        ArrayList<Pair<String, Class>> array = new ArrayList<>();
        array.add(new Pair<String, Class>(getString(R.string.alerts), AlertsActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.geofencing), GeofencingActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.reports), ReportsActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.ruler), RulerActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.poi), POIActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.showPoint), ShowPointActivity.class));
        array.add(new Pair<String, Class>(getString(R.string.sendCommand), SendCommandActivity.class));

        adapter.setArray(array);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AwesomeAdapter<Pair> adapter = (AwesomeAdapter<Pair>) listview.getAdapter();
                try {
                    Intent intent = new Intent(ToolsActivity.this, (Class<?>) adapter.getArray().get(position).second);
                    startActivity(intent);
                } catch(Exception e)
                {
                    Toast.makeText(ToolsActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
