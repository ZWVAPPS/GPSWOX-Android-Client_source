package com.gpswox.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.gpswox.android.R;
import com.gpswox.android.models.Event;

import java.util.ArrayList;

/**
 * Created by gintas on 29/11/15.
 */
public class EventsAdapter extends AwesomeAdapter<Event> {
    public EventsAdapter(Context context) {
        super(context);
    }

    ArrayList<Event> original;
    @Override
    public void setArray(ArrayList<Event> array) {
        super.setArray(array);
        if(original == null)
            original = array;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = getLayoutInflater().inflate(R.layout.adapter_events, null);

        Event item = getItem(position);
        TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
        device_name.setText(item.device_name);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(item.time);
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText(item.message);
        TextView geofence_name = (TextView) convertView.findViewById(R.id.geofence_name);
        geofence_name.setText(item.geofence_name);

        return convertView;
    }

    ItemFilter mFilter = new ItemFilter();
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final ArrayList<Event> nlist = new ArrayList<>();

            for(Event item : original)
            {
                if(item.fitForFilter(filterString))
                    nlist.add(item);
            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setArray((ArrayList<Event>) results.values);
            notifyDataSetChanged();
        }

    }
}
