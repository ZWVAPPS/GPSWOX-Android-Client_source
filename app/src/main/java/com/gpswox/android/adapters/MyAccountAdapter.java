package com.gpswox.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gpswox.android.R;
import com.gpswox.android.api.ApiInterface;

import java.util.ArrayList;

/**
 * Created by gintas on 08/10/15.
 */
public class MyAccountAdapter extends BaseExpandableListAdapter
{
    public Context context;
    public LayoutInflater inflater;
    private ApiInterface.GetMyAccountDataResult accountData;
    private String ip;

    public MyAccountAdapter(Context context, ApiInterface.GetMyAccountDataResult accountData)
    {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.accountData = accountData;
    }
    @Override
    public int getGroupCount() {
        ip = context.getString(R.string.ip);
        return ip.isEmpty() ? 2 : 1;
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
            case 0: titleText = context.getString(R.string.membership); break;
            case 1: titleText = context.getString(R.string.changePassword); break;
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
        if(groupPosition == 0) {
            convertView = inflater.inflate(R.layout.adapter_myaccount_membership, null);
            ListView listview = (ListView) convertView.findViewById(R.id.listview);
            ArrayList<Pair> array = new ArrayList<>();
            array.add(new Pair<>(context.getString(R.string.email)+":", accountData.email));
            array.add(new Pair<>(context.getString(R.string.membership)+":", accountData.plan));
            if (accountData.devices_limit > 0) {
                array.add(new Pair<>(context.getString(R.string.devicesLimit)+":", accountData.devices_limit));
            }
            if (accountData.expiration_date != null) {
                array.add(new Pair<>(context.getString(R.string.expirationDate)+":", accountData.expiration_date));
            }
            listview.setAdapter(new AwesomeAdapter<Pair>(context, array)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null)
                        convertView = inflater.inflate(R.layout.adapter_info_list, null);
                    Pair item = getItem(position);
                    TextView left = (TextView) convertView.findViewById(R.id.left);
                    TextView right = (TextView) convertView.findViewById(R.id.right);

                    left.setText(item.first != null ? String.valueOf(item.first) : "");
                    right.setText(item.second != null ? String.valueOf(item.second) : "");
                    return convertView;
                }
            });

            if (ip.isEmpty()) {
                convertView.findViewById(R.id.changeMembership).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gpswox.com/en/gps-trackers-shop/gps-tracking-and-fleet-management-system-1?email=" + accountData.email));
                        context.startActivity(browserIntent);
                    }
                });
            }
            else {
                convertView.findViewById(R.id.changeMembership).setVisibility(View.INVISIBLE);
            }
        }
        else if(groupPosition == 1) {
            convertView = inflater.inflate(R.layout.adapter_myaccount_changepassword, null);
            convertView.findViewById(R.id.openBrowser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gpswox.com/en/change_password?email=" + accountData.email));
                    context.startActivity(browserIntent);
                }
            });
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
