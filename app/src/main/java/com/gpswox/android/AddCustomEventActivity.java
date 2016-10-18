package com.gpswox.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gpswox.android.adapters.AwesomeAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.models.CustomEventCondition;
import com.gpswox.android.models.CustomEventConditionType;
import com.gpswox.android.models.CustomEventProtocol;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddCustomEventActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.showAlways) CheckBox showAlways;
    @Bind(R.id.protocol) Spinner protocol;
    @Bind(R.id.message) EditText message;
    @Bind(R.id.addCustomEvent) View addCustomEvent;
    @Bind(R.id.contentLayout) View contentLayout;
    @Bind(R.id.list) ListView list;

    // add parameter items
    @Bind(R.id.paramName) EditText paramName;
    @Bind(R.id.comparison) Spinner comparison;
    @Bind(R.id.paramValue) EditText paramValue;
    @Bind(R.id.addCondition) View addCondition;

    AwesomeAdapter<CustomEventCondition> conditionsAdapter;
    ArrayList<CustomEventConditionType> types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_event);
        ButterKnife.bind(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getCustomEventData(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.GetCustomEventDataResult>() {
            @Override
            public void success(ApiInterface.GetCustomEventDataResult getCustomEventDataResult, Response response)
            {
                ArrayAdapter<CustomEventProtocol> protocolsAdapter = new ArrayAdapter<>(AddCustomEventActivity.this, R.layout.spinner_item, getCustomEventDataResult.protocols);
                protocol.setAdapter(protocolsAdapter);

                ArrayAdapter<CustomEventConditionType> typesAdapter = new ArrayAdapter<>(AddCustomEventActivity.this, R.layout.spinner_item, getCustomEventDataResult.types);
                comparison.setAdapter(typesAdapter);
                types = getCustomEventDataResult.types;

                contentLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(AddCustomEventActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        addCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomEventCondition item = new CustomEventCondition();
                item.tag = paramName.getText().toString();
                item.type = ((CustomEventConditionType) comparison.getSelectedItem()).id;
                item.tag_value = paramValue.getText().toString();
                conditionsAdapter.add(item);

                paramName.setText("");
                paramValue.setText("");
                comparison.setSelection(0);
            }
        });
        conditionsAdapter = new AwesomeAdapter<CustomEventCondition>(this, new ArrayList<CustomEventCondition>())
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null);
                    convertView = getLayoutInflater().inflate(R.layout.adapter_customevents_condition, null);
                final CustomEventCondition item = getItem(position);
                TextView text = (TextView) convertView.findViewById(R.id.text);
                String typeValue = "";
                for(CustomEventConditionType type : types)
                    if(type.id.equals(item.type))
                        typeValue = type.value;
                text.setText(String.format("%s %s %s", item.tag, typeValue, item.tag_value));

                convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        };
        list.setAdapter(conditionsAdapter);


        addCustomEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String protocolStr = ((CustomEventProtocol) protocol.getSelectedItem()).id;
                String messageStr = message.getText().toString();
                int show_always = showAlways.isChecked() ? 1 : 0;

                String conditions_array = new Gson().toJson(conditionsAdapter.getArray());

                API.getApiInterface(AddCustomEventActivity.this).addCustomEvent(api_key, Lang.getCurrentLanguage(), protocolStr, messageStr, show_always, conditions_array, new Callback<ApiInterface.AddCustomEventResult>() {
                    @Override
                    public void success(ApiInterface.AddCustomEventResult addCustomEventResult, Response response)
                    {
                        Intent data = new Intent();
                        data.putExtra("item", new Gson().toJson(addCustomEventResult.item));
                        setResult(RESULT_OK, data);
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(AddCustomEventActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
