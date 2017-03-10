package com.gpswox.android.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.gpswox.android.R;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.Geofence;
import com.gpswox.android.models.Report;
import com.gpswox.android.models.ReportFormat;
import com.gpswox.android.models.ReportStop;
import com.gpswox.android.models.ReportType;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gintas on 08/10/15.
 */
public class EditReportAdapter extends BaseExpandableListAdapter
{
    public Context context;
    public Report report;
    public ArrayList<ReportType> types;
    public ArrayList<Device> devicesArray;
    public ArrayList<Geofence> geofencesArray;
    public LayoutInflater inflater;

    ApiInterface.GetReportDataResult data;

    public EditReportAdapter(Context context, Report report, ArrayList<ReportType> types, ApiInterface.GetReportDataResult getReportDataResult)
    {
        this.context = context;
        this.report = report;
        this.types = types;
        this.devicesArray = getReportDataResult.devices;
        this.geofencesArray = getReportDataResult.geofences;
        this.data = getReportDataResult;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if(convertView == null)
            convertView = inflater.inflate(R.layout.adapter_expandable_parent, null);
        String titleText = "";
        switch(groupPosition)
        {
            case 0: titleText = context.getString(R.string.report); break;
            case 1: titleText = context.getString(R.string.schedule); break;
            case 2: titleText = context.getString(R.string.timePeriod); break;
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
        if(groupPosition == 0) { // report
            convertView = inflater.inflate(R.layout.adapter_editreport_report, null);

            // title
            EditText title = (EditText) convertView.findViewById(R.id.titleEditText);
            title.setText(report.title);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    report.title = s.toString();
                }
            });
            // speed limit
            EditText speedLimit = (EditText) convertView.findViewById(R.id.speedLimitEditText);
            speedLimit.setText(String.valueOf(report.speed_limit));
            speedLimit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(StringUtils.isNotEmpty(s))
                    report.speed_limit = Integer.valueOf(s.toString());
                }
            });


            // type
            Spinner type = (Spinner) convertView.findViewById(R.id.typeSpinner);
            ArrayAdapter<ReportType> typesAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, types);
            type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    report.type = types.get(position).id;
                }
            });
            type.setAdapter(typesAdapter);
            for (int i = 0; i < types.size(); i++) {
                ReportType reportType = types.get(i);
                if(reportType.id == report.type)
                    type.setSelection(i);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // from

            report.dateFrom = dateFormat.format(calendar.getTime());
            final TextView fromDateTextView = (TextView) convertView.findViewById(R.id.dateFrom);
            fromDateTextView.setText(dateFormat.format(calendar.getTime()));
            fromDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    try {
                        new SlideDateTimePicker.Builder(((AppCompatActivity)context).getSupportFragmentManager())
                                .setListener(new SlideDateTimeListener() {
                                    @Override
                                    public void onDateTimeSet(Date date)
                                    {
                                        fromDateTextView.setText(dateFormat.format(date));
                                        report.dateFrom = fromDateTextView.getText().toString();
                                    }
                                })
                                .setInitialDate(dateFormat.parse(fromDateTextView.getText().toString()))
                                .setIs24HourTime(true)
                                .build()
                                .show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            // to

            final TextView toDateTextView = (TextView) convertView.findViewById(R.id.dateTo);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 45);
            calendar.set(Calendar.SECOND, 0);
            report.dateTo = dateFormat.format(calendar.getTime());

            toDateTextView.setText(dateFormat.format(calendar.getTime()));
            toDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    try {
                        new SlideDateTimePicker.Builder(((AppCompatActivity)context).getSupportFragmentManager())
                                .setListener(new SlideDateTimeListener() {
                                    @Override
                                    public void onDateTimeSet(Date date)
                                    {
                                        toDateTextView.setText(dateFormat.format(date));
                                        report.dateTo = toDateTextView.getText().toString();
                                    }
                                })
                                .setInitialDate(dateFormat.parse(toDateTextView.getText().toString()))
                                .setIs24HourTime(true)
                                .build()
                                .show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });


            // format
            final Spinner format = (Spinner) convertView.findViewById(R.id.formatSpinner);
            ArrayAdapter<ReportFormat> formatAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, data.formats);
            format.setAdapter(formatAdapter);
            format.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    report.format = ((ReportFormat) format.getSelectedItem()).id;
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
            for(int i = 0; i < data.formats.size(); i++)
                if(report.format.toLowerCase().equals(data.formats.get(i).id))
                    format.setSelection(i);

            // stops
            final Spinner stops = (Spinner) convertView.findViewById(R.id.stopsSpinner);
            ArrayAdapter<ReportStop> stopsAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, data.stops);
            stops.setAdapter(stopsAdapter);
            stops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    report.stops = ((ReportStop) stops.getSelectedItem()).id;
                }
            });
            for(int i = 0; i < data.stops.size(); i++)
                if(report.stops == data.stops.get(i).id)
                    stops.setSelection(i);

            // devices
            final TextView devices = (TextView) convertView.findViewById(R.id.devicesTextView);
            devices.setText(String.valueOf(report.devices.size()));
            devices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.selectDevices);
                    String[] devicesStrings = new String[devicesArray.size()];
                    boolean[] checkedItems = new boolean[devicesArray.size()];
                    for(int i = 0; i < devicesArray.size(); i++)
                    {
                        devicesStrings[i] = devicesArray.get(i).name;
                        checkedItems[i] = report.devices.contains(devicesArray.get(i).id);
                    }
                    builder.setMultiChoiceItems(devicesStrings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Device device = devicesArray.get(which);
                            if(isChecked)
                            {
                                if(!report.devices.contains(device.id))
                                    report.devices.add(device.id);
                            }
                            else
                            if(report.devices.contains(device.id))
                                report.devices.remove((Object)device.id);

                            devices.setText(String.valueOf(report.devices.size()));
                        }
                    });
                    builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });

            // geofences
            final TextView geofences = (TextView) convertView.findViewById(R.id.geofencesTextView);
            geofences.setText(String.valueOf(report.geofences.size()));
            geofences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.selectGeofences);
                    String[] geofencesString = new String[geofencesArray.size()];
                    boolean[] checkedItems = new boolean[geofencesArray.size()];
                    for(int i = 0; i < geofencesArray.size(); i++)
                    {
                        geofencesString[i] = geofencesArray.get(i).name;
                        checkedItems[i] = report.geofences.contains(geofencesArray.get(i).id);
                    }
                    builder.setMultiChoiceItems(geofencesString, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Geofence geofence = geofencesArray.get(which);
                            if(isChecked)
                            {
                                if(!report.geofences.contains(geofence.id))
                                    report.geofences.add(geofence.id);
                            }
                            else
                            if(report.geofences.contains(geofence.id))
                                report.geofences.remove((Object)geofence.id);

                            geofences.setText(String.valueOf(report.geofences.size()));
                        }
                    });
                    builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });

        }
        else if(groupPosition == 1) { // schedule
            convertView = inflater.inflate(R.layout.adapter_editreport_schedule, null);

            final Button daily = (Button) convertView.findViewById(R.id.daily);
            if(report.daily > 0) {
                daily.setBackgroundResource(R.drawable.button_selectable_left_selected);
                daily.setTextColor(0xFFFFFFFF);
            }
            else {
                daily.setBackgroundResource(R.drawable.button_selectable_left_unselected);
                daily.setTextColor(0xFF666666);
            }

            daily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(report.daily > 0)
                        report.daily = 0;
                    else
                        report.daily = 1;

                    if(report.daily > 0) {
                        daily.setBackgroundResource(R.drawable.button_selectable_left_selected);
                        daily.setTextColor(0xFFFFFFFF);
                    }
                    else {
                        daily.setBackgroundResource(R.drawable.button_selectable_left_unselected);
                        daily.setTextColor(0xFF666666);
                    }
                }
            });

            final Button weekly = (Button) convertView.findViewById(R.id.weekly);
            if(report.weekly > 0) {
                weekly.setBackgroundResource(R.drawable.button_selectable_right_selected);
                weekly.setTextColor(0xFFFFFFFF);
            }
            else {
                weekly.setBackgroundResource(R.drawable.button_selectable_right_unselected);
                weekly.setTextColor(0xFF666666);
            }

            weekly.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(report.weekly > 0)
                        report.weekly = 0;
                    else
                        report.weekly = 1;

                    if(report.weekly > 0) {
                        weekly.setBackgroundResource(R.drawable.button_selectable_right_selected);
                        weekly.setTextColor(0xFFFFFFFF);
                    }
                    else {
                        weekly.setBackgroundResource(R.drawable.button_selectable_right_unselected);
                        weekly.setTextColor(0xFF666666);
                    }
                }
            });

            EditText email = (EditText) convertView.findViewById(R.id.email);
            email.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    report.email = s.toString();
                }
            });
            email.setText(report.email);
        }
        else if(groupPosition == 2) // time period
        {
            convertView = inflater.inflate(R.layout.adapter_editreport_timeperiod, null);

            final Spinner filter = (Spinner) convertView.findViewById(R.id.filterSpinner);
            ArrayAdapter<ReportStop> filterAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, data.stops);
            filter.setAdapter(filterAdapter);
            TextView from = (TextView) convertView.findViewById(R.id.from);
            from.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            TextView to = (TextView) convertView.findViewById(R.id.to);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public Report getReport() {
        return report;
    }
}
