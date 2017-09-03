package com.gpswox.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.HistoryItem;
import com.gpswox.android.models.HistoryItemCoord;
import com.gpswox.android.models.HistorySensor;
import com.gpswox.android.models.HistorySensorData;
import com.gpswox.android.models.PrecalculatedGraphData;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Utils;
import com.squareup.picasso.Picasso;

import org.osmdroid.util.GeoPoint;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private static final String TAG = "HistoryActivity";
    @Bind(R.id.back)
    View back;
    @Bind(R.id.search)
    View search;
    @Bind(R.id.searchLayout)
    View search_layout;
    @Bind(R.id.startSearch)
    View startSearch;

    @Bind(R.id.deviceSpinner)
    Spinner deviceSpinner;
    @Bind(R.id.fromDateTextView)
    TextView fromDateTextView;
    @Bind(R.id.toDateTextView)
    TextView toDateTextView;

    // list layout'as
    @Bind(R.id.list_layout)
    View list_layout;
    @Bind(R.id.list_layout_list)
    ListView list_layout_list;
    @Bind(R.id.list_layout_map)
    View list_layout_map;
    @Bind(R.id.list_layout_statistics)
    View list_layout_statistics;

    // map layoutas
    @Bind(R.id.map_layout)
    View map_layout;
    @Bind(R.id.zoom_in)
    View zoom_in;
    @Bind(R.id.zoom_out)
    View zoom_out;
    @Bind(R.id.list_layout_historylog)
    View list_layout_historylog;
    @Bind(R.id.list_layout_statistics2)
    View list_layout_statistics2;

    // stats layout'as
    @Bind(R.id.stats_layout)
    View stats_layout;
    @Bind(R.id.stats_layout_list)
    ExpandableListView stats_list;
    @Bind(R.id.list_layout_map2)
    View list_layout_map2;
    @Bind(R.id.list_layout_historylog2)
    View list_layout_historylog2;

    @Bind(R.id.loading_layout)
    View loading_layout;
    @Bind(R.id.nodata_layout)
    View nodata_layout;

    AwesomeAdapter<HistoryItem> historyLogAdapter;

    ArrayList<HistoryItem> historyItems;
    ApiInterface.GetHistoryResult getHistoryResult;

    ArrayList<HistoryItemCoord> historyItemCoords;

    ArrayList<PrecalculatedGraphData> precalculatedGraphDatas;

    private GoogleMap map;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
        search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                if (search_layout.getVisibility() == View.VISIBLE)
                    search_layout.setVisibility(View.GONE);
                else
                    search_layout.setVisibility(View.VISIBLE);
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        fromDateTextView.setText(dateFormat.format(calendar.getTime()));
        fromDateTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    new SlideDateTimePicker.Builder(getSupportFragmentManager())
                            .setListener(new SlideDateTimeListener()
                            {
                                @Override
                                public void onDateTimeSet(Date date)
                                {
                                    fromDateTextView.setText(dateFormat.format(date));
                                }
                            })
                            .setInitialDate(dateFormat.parse(fromDateTextView.getText().toString()))
                            .setIs24HourTime(true)
                            .build()
                            .show();
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
        });

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);

        toDateTextView.setText(dateFormat.format(calendar.getTime()));
        toDateTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    new SlideDateTimePicker.Builder(getSupportFragmentManager())
                            .setListener(new SlideDateTimeListener()
                            {
                                @Override
                                public void onDateTimeSet(Date date)
                                {
                                    toDateTextView.setText(dateFormat.format(date));
                                }
                            })
                            .setInitialDate(dateFormat.parse(toDateTextView.getText().toString()))
                            .setIs24HourTime(true)
                            .build()
                            .show();
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
        });

        API.getApiInterface(this).getDevices((String) DataSaver.getInstance(HistoryActivity.this).load("api_key"), getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>()
        {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                ArrayList<Device> totalDevices = new ArrayList<>();
                for (ApiInterface.GetDevicesItem item : getDevicesItems)
                    totalDevices.addAll(item.items);
                final ArrayAdapter<Device> devicesAdapter = new ArrayAdapter<>(HistoryActivity.this, R.layout.spinner_item, totalDevices);
                deviceSpinner.setAdapter(devicesAdapter);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(HistoryActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        startSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Date fromDate = dateFormat.parse(fromDateTextView.getText().toString());
                    String from_date = new SimpleDateFormat("yyyy-MM-dd").format(fromDate);
                    String from_time = new SimpleDateFormat("HH:mm").format(fromDate);

                    Date toDate = dateFormat.parse(toDateTextView.getText().toString());
                    String to_date = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
                    String to_time = new SimpleDateFormat("HH:mm").format(toDate);

                    long diffInMillies = toDate.getTime() - fromDate.getTime();
                    long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    if (days > 31)
                    {
                        Toast.makeText(HistoryActivity.this, "Maximum interval is one month.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    nodata_layout.setVisibility(View.GONE);
                    list_layout.setVisibility(View.GONE);
                    map_layout.setVisibility(View.GONE);
                    stats_layout.setVisibility(View.GONE);
                    loading_layout.setVisibility(View.VISIBLE);
                    int device_id = ((Device) deviceSpinner.getSelectedItem()).id;

                    API.getApiInterface(HistoryActivity.this).getHistory((String) DataSaver.getInstance(HistoryActivity.this).load("api_key"), getResources().getString(R.string.lang),
                            device_id, from_date, from_time, to_date, to_time, false,
                            new Callback<ApiInterface.GetHistoryResult>()
                            {
                                @Override
                                public void success(ApiInterface.GetHistoryResult result, Response response)
                                {
                                    if (result.items == null)
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        nodata_layout.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    if (result.items.size() != 0)
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        list_layout.setVisibility(View.VISIBLE);
                                    } else
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        nodata_layout.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    historyItems = result.items;
                                    search_layout.setVisibility(View.GONE);
                                    getHistoryResult = result;
                                    historyLogAdapter.setArray(getHistoryResult.items);
                                    initMap(result.items);

                                    if (getHistoryResult.sensors == null)
                                        getHistoryResult.sensors = new ArrayList<HistorySensor>();

                                    getHistoryResult.sensors.add(0, new HistorySensor("speed", "Speed", " " + DataSaver.getInstance(HistoryActivity.this).load("unit_of_distance")));
                                    getHistoryResult.sensors.add(1, new HistorySensor("altitude", "Altitude", " " + DataSaver.getInstance(HistoryActivity.this).load("unit_of_altitude")));

                                    historyItemCoords = new ArrayList<>();
                                    for (HistoryItem item : getHistoryResult.items)
                                        historyItemCoords.addAll(item.items);
                                    Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
                                    {
                                        @Override
                                        public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
                                        {
                                            long t1 = rhs.getTimestamp();
                                            long t2 = lhs.getTimestamp();
                                            if (t2 > t1)
                                                return 1;
                                            else if (t1 > t2)
                                                return -1;
                                            else
                                                return 0;
                                        }
                                    });

                                    precalculatedGraphDatas = new ArrayList<>();
                                    for (HistorySensor sensor : getHistoryResult.sensors)
                                    {
                                        ArrayList<Float> sensorDataValues = new ArrayList<>();
                                        ArrayList<Long> sensorDataTimestamps = new ArrayList<>();
                                        for (HistoryItemCoord item : historyItemCoords)
                                        {
                                            if (item.sensors_data != null)
                                                for (HistorySensorData data : item.sensors_data)
                                                    if (data.id.equals(sensor.id))
                                                    {
                                                        sensorDataValues.add(data.value);
                                                        long timestamp = item.getTimestamp();
                                                        sensorDataTimestamps.add(timestamp);
                                                    }
                                        }

                                        PrecalculatedGraphData object = new PrecalculatedGraphData();
                                        object.sensor_id = sensor.id;
                                        object.sensorDataValues = sensorDataValues;
                                        object.sensorDataTimestamps = sensorDataTimestamps;

                                        ArrayList<String> xVals = new ArrayList<>();
                                        for (int i = 0; i < sensorDataTimestamps.size(); i++)
                                            xVals.add(new SimpleDateFormat("MM-dd HH:mm:ss").format(sensorDataTimestamps.get(i)));
                                        object.xVals = xVals;

                                        ArrayList<Entry> yVals = new ArrayList<>();
                                        for (int i = 0; i < sensorDataValues.size(); i++)
                                            yVals.add(new Entry(sensorDataValues.get(i), i));
                                        object.yVals = yVals;

                                        precalculatedGraphDatas.add(object);
                                    }

                                    loading_layout.setVisibility(View.GONE);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError)
                                {
                                    loading_layout.setVisibility(View.GONE);
                                    Log.d(TAG, "get history failure: " + retrofitError.getMessage());
                                    if (retrofitError.getKind() == RetrofitError.Kind.UNEXPECTED)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.tooMuchData, Toast.LENGTH_LONG).show();
                                    }
                                    else if(retrofitError.getKind() == RetrofitError.Kind.NETWORK)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                                    }
                                    else if (retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() == 403)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                                    } else
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        // History log list
        historyLogAdapter = new AwesomeAdapter<HistoryItem>(this)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_historylog, null);
                HistoryItem item = getItem(position);
                TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
                device_name.setText(getHistoryResult.device.name);
                TextView date = (TextView) convertView.findViewById(R.id.date);

                String dateText = item.raw_time;
                TextView hint = (TextView) convertView.findViewById(R.id.hint);
                String hintString = item.getHint(getHistoryResult.item_class);
                switch (hintString){
                    case "drive":
                        dateText += " (" + item.show + ")";
                        hint.setText(R.string.driving);
                        break;
                    case "stop":
                        dateText += " (" + item.show + ")";
                        hint.setText(R.string.stopped);
                        break;
                    case "start":
                        dateText += " (" + item.show + ")";
                        hint.setText(R.string.route_begin);
                        break;
                    case "end":
                        dateText += " (" + item.show + ")";
                        hint.setText(R.string.route_end);
                        break;
                    case "event":
                        dateText += " (" + item.show + ")";
                        hint.setText(R.string.event);
                        break;
                }

                date.setText(dateText);

                ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
                Picasso.with(HistoryActivity.this).load(item.getImageUrl(getHistoryResult.images)).into(icon);
                return convertView;
            }
        };
        list_layout_list.setAdapter(historyLogAdapter);

        list_layout_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(HistoryActivity.this, HistoryItemDetailsActivity.class);
                intent.putExtra("item", new Gson().toJson(list_layout_list.getItemAtPosition(position)));
                intent.putExtra("historyItemClasses", new Gson().toJson(getHistoryResult.item_class));
                intent.putExtra("historyItemImages", new Gson().toJson(getHistoryResult.images));
                startActivity(intent);
            }
        });

        stats_list.setAdapter(new BaseExpandableListAdapter()
        {
            @Override
            public int getGroupCount()
            {
                return 2;
            }

            @Override
            public int getChildrenCount(int groupPosition)
            {
                if (groupPosition == 0) return 1;
                else return historyItemCoords.size();
            }

            @Override
            public Object getGroup(int groupPosition)
            {
                return null;
            }

            @Override
            public Object getChild(int groupPosition, int childPosition)
            {
                return null;
            }

            @Override
            public long getGroupId(int groupPosition)
            {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition)
            {
                return 0;
            }

            @Override
            public boolean hasStableIds()
            {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_expandable_parent, null);
                String titleText = "";
                switch (groupPosition)
                {
                    case 0:
                        titleText = getString(R.string.statisticsGraph);
                        break;
                    case 1:
                        titleText = getString(R.string.dataLog);
                        break;
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(titleText);

                ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
                expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
            {
                if (groupPosition == 0)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_historystats_graph, null);
                    final Spinner dataSpinner = (Spinner) convertView.findViewById(R.id.dataSpinner);
                    final ArrayAdapter<HistorySensor> dataAdapter = new ArrayAdapter<>(HistoryActivity.this, R.layout.spinner_item, getHistoryResult.sensors);
                    dataSpinner.setAdapter(dataAdapter);

                    final LineChart chart = (LineChart) convertView.findViewById(R.id.chart);
                    dataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onNothingSelected(AdapterView<?> parent)
                        {
                        }

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                        {
                            HistorySensor selectedSensor = (HistorySensor) dataSpinner.getItemAtPosition(position);
                            updateChart(chart, selectedSensor);
                        }
                    });
                    chart.setGridBackgroundColor(0xFFf5f5f5);
                    chart.setDrawGridBackground(true);
                    chart.setDescription("");
                    chart.setTouchEnabled(true);
                    chart.setDragEnabled(true);
                    chart.setScaleEnabled(true);
                    chart.setPinchZoom(true);
                    updateChart((LineChart) convertView.findViewById(R.id.chart), getHistoryResult.sensors.get(0));

                    convertView.findViewById(R.id.chart).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            HistorySensor selectedSensor = (HistorySensor) dataSpinner.getItemAtPosition(dataSpinner.getSelectedItemPosition());
                            PrecalculatedGraphData precalculatedGraphData = null;
                            for (PrecalculatedGraphData item : precalculatedGraphDatas)
                                if (item.sensor_id.equals(selectedSensor.id))
                                    precalculatedGraphData = item;

                            Intent intent = new Intent(HistoryActivity.this, ViewHistoryChartActivity.class);
                            intent.putExtra("precalculatedGraphData", new Gson().toJson(precalculatedGraphData));
                            intent.putExtra("selectedSensor", new Gson().toJson(selectedSensor));
                            startActivity(intent);
                        }
                    });

                    convertView.findViewById(R.id.graph_zoom_in).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            chart.zoomIn();
                        }
                    });
                    convertView.findViewById(R.id.graph_zoom_out).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            chart.zoomOut();
                        }
                    });
                } else if (groupPosition == 1)
                {
                    convertView = getLayoutInflater().inflate(R.layout.adapter_historyitemcoord, null);
                    final HistoryItemCoord item = historyItemCoords.get(childPosition);
                    TextView time = (TextView) convertView.findViewById(R.id.time);
                    time.setText(item.raw_time);
                    TextView speed = (TextView) convertView.findViewById(R.id.speed);
                    if (item.sensors_data != null)
                    {
                        for (HistorySensorData data : item.sensors_data)
                            if (data.id.toLowerCase().equals("speed"))
                                speed.setText(String.valueOf(data.value));
                    }

                    convertView.findViewById(R.id.details).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(HistoryActivity.this, HistoryItemCoordDetailsActivity.class);
                            intent.putExtra("item", new Gson().toJson(item));
                            intent.putExtra("sensors", new Gson().toJson(getHistoryResult.sensors));
                            startActivity(intent);
                        }
                    });
                }
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition)
            {
                return false;
            }
        });

        // BOTTOM BUTTON'AI
        View.OnClickListener mapClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                list_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.GONE);
                map_layout.setVisibility(View.VISIBLE);

                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (HistoryItemCoord coord : historyItemCoords)
                            builder.include(new LatLng(Double.valueOf(coord.lat), Double.valueOf(coord.lng)));

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
                    }
                });
            }
        };
        list_layout_map.setOnClickListener(mapClick);
        list_layout_map2.setOnClickListener(mapClick);

        View.OnClickListener historyLogClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                map_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.GONE);
                list_layout.setVisibility(View.VISIBLE);
            }
        };
        list_layout_historylog.setOnClickListener(historyLogClick);
        list_layout_historylog2.setOnClickListener(historyLogClick);

        View.OnClickListener statisticsClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                map_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.VISIBLE);
                list_layout.setVisibility(View.GONE);
            }
        };
        list_layout_statistics.setOnClickListener(statisticsClick);
        list_layout_statistics2.setOnClickListener(statisticsClick);


        zoom_in.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    private void updateChart(LineChart chart, final HistorySensor selectedSensor)
    {
        PrecalculatedGraphData precalculatedGraphData = null;
        for (PrecalculatedGraphData item : precalculatedGraphDatas)
            if (item.sensor_id.equals(selectedSensor.id))
                precalculatedGraphData = item;

        if (precalculatedGraphData == null) return;

        chart.clear();
        chart.setHighlightPerTapEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(0xFFf5f5f5);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.setValueFormatter(new YAxisValueFormatter()
        {
            @Override
            public String getFormattedValue(float value, YAxis yAxis)
            {
                return String.valueOf(Math.round(value)) + selectedSensor.sufix;
            }
        });
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(0xFFb2b2b2);
        chart.getXAxis().setTextColor(0xFFb2b2b2);
        chart.getXAxis().setGridColor(0xFFeaeaea);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisLeft().setGridColor(0xFFeaeaea);
        chart.getAxisLeft().setLabelCount(4, true);
        chart.getAxisLeft().setAxisMinValue(0f);


        LineDataSet set1 = new LineDataSet(precalculatedGraphData.yVals, selectedSensor.name);

        set1.setColor(0xFF9e9e9e);
        set1.setDrawCircles(false);
        set1.setLineWidth(1f);
        set1.setDrawValues(false);
        set1.setFillAlpha(100);
        set1.setFillColor(0xFFdddddd);
        set1.setDrawFilled(true);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(precalculatedGraphData.xVals, dataSets);
        chart.setData(data);
    }

    private void initMap(final ArrayList<HistoryItem> items)
    {
        map.clear();
        ArrayList<HistoryItemCoord> historyItemCoords = new ArrayList<>();
        for (HistoryItem item : items)
        {
            for (HistoryItemCoord coord : item.items)
            {
                historyItemCoords.add(coord);
            }
        }
        Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
        {
            @Override
            public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
            {
                long t1 = lhs.getTimestamp();
                long t2 = rhs.getTimestamp();
                if (t2 > t1)
                    return 1;
                else if (t1 > t2)
                    return -1;
                else
                    return 0;
            }
        });
        final List<GeoPoint> points = new ArrayList<>();

        Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
        {
            @Override
            public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
            {
                if (lhs.getTimestamp() == rhs.getTimestamp())
                    return 0;
                else if (lhs.getTimestamp() < rhs.getTimestamp())
                    return -1;
                return 1;
            }
        });

        long previousCoordTime = historyItemCoords.get(0).getTimestamp();
        int loopId = 0;
        for (HistoryItemCoord coord : historyItemCoords)
        {
            if (loopId == 0 || (loopId > 0 && previousCoordTime != coord.getTimestamp()))
            {
                GeoPoint point = new GeoPoint(Double.parseDouble(coord.lat), Double.parseDouble(coord.lng));
                points.add(point);
            }
            previousCoordTime = coord.getTimestamp();
            loopId++;
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#819afc"));
        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
        for (GeoPoint point : points)
        {

            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));

            /*MarkerOptions opt = new MarkerOptions();
            opt.position(new LatLng(point.getLatitude(), point.getLongitude()));
            opt.title(String.valueOf(id)).snippet(String.valueOf(point.getLatitude()) + ", " + point.getLongitude() + "\n" + historyItemCoords.get(id).getTimestamp() + "\n" + historyItemCoords.get(0).raw_time);
            id++;
            map.addMarker(opt);*/
        }
        map.addPolyline(polylineOptions);

        // Create markers
        new AsyncTask<Void, Void, Void>()
        {
            ArrayList<MarkerOptions> items;

            @Override
            protected Void doInBackground(Void... params)
            {
                int markerSize = Utils.dpToPx(HistoryActivity.this, 25);
                items = new ArrayList<>();
                for (HistoryItem item : getHistoryResult.items)
                {
                    if (item.status == 1) continue;
                    try
                    {
                        Bitmap bmp = BitmapFactory.decodeStream(new URL(item.getImageUrl(getHistoryResult.images)).openConnection().getInputStream());

                        int srcWidth = bmp.getWidth();
                        int srcHeight = bmp.getHeight();

                        int maxWidth = markerSize;
                        int maxHeight = markerSize;

                        float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                        int dstWidth = (int) (srcWidth * ratio);
                        int dstHeight = (int) (srcHeight * ratio);

                        bmp = bmp.createScaledBitmap(bmp, markerSize, markerSize, true);
                        MarkerOptions opt = new MarkerOptions();
                        opt.position(new LatLng(Float.valueOf(item.items.get(0).lat), Float.valueOf(item.items.get(0).lng)));
                        opt.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                        items.add(opt);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                for (MarkerOptions opt : items)
                    map.addMarker(opt);

                Log.d(TAG, "onPostExecute: icons downloaded and added to map, total markers: " + items.size());
            }
        }.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
    }
}
