package com.gpswox.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.google.gson.Gson;
import com.gpswox.android.models.HistorySensor;
import com.gpswox.android.models.PrecalculatedGraphData;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewHistoryChartActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.chart) LineChart chart;

    private ArrayList<PrecalculatedGraphData> precalculatedGraphDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history_chart);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        chart.setGridBackgroundColor(0xFFf5f5f5);
        chart.setDrawGridBackground(true);
        chart.setDescription("");
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setHighlightPerTapEnabled(false);

        final HistorySensor selectedSensor = new Gson().fromJson(getIntent().getStringExtra("selectedSensor"), HistorySensor.class);
        PrecalculatedGraphData precalculatedGraphData = new Gson().fromJson(getIntent().getStringExtra("precalculatedGraphData"), PrecalculatedGraphData.class);

        if(precalculatedGraphData == null) return;

        chart.clear();
        chart.setHighlightPerTapEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(0xFFf5f5f5);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return String.valueOf(Math.round(value)) + selectedSensor.sufix;
            }
        });
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(0xFFb2b2b2);
        chart.getXAxis().setTextColor(0xFFb2b2b2);
        chart.getXAxis().setGridColor(0xFFeaeaea);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisLeft().setGridColor(0xFFeaeaea);
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
}
