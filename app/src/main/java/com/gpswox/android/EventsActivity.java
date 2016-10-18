package com.gpswox.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gpswox.android.adapters.EventsAdapter;
import com.gpswox.android.api.API;
import com.gpswox.android.api.ApiInterface;
import com.gpswox.android.utils.DataSaver;
import com.gpswox.android.utils.Lang;
import com.gpswox.android.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EventsActivity extends AppCompatActivity
{
    @Bind(R.id.back) View back;
    @Bind(R.id.list) ListView list;
    @Bind(R.id.clearAllEvents) View clearAllEvents;
    @Bind(R.id.content_layout) View content_layout;
    @Bind(R.id.loading_layout) View loading_layout;
    @Bind(R.id.nodata_layout) View nodata_layout;
    @Bind(R.id.search) View search;
    String searchtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        ButterKnife.bind(this);

        final String api_key = (String) DataSaver.getInstance(EventsActivity.this).load("api_key");
        final EventsAdapter adapter = new EventsAdapter(this);
        list.setAdapter(adapter);

        loading_layout.setVisibility(View.VISIBLE);
        API.getApiInterface(this).getEvents(api_key, Lang.getCurrentLanguage(), 0, new Callback<ApiInterface.GetEventsResult>() {
            @Override
            public void success(ApiInterface.GetEventsResult result, Response response)
            {
                adapter.setArray(result.items.data);

                loading_layout.setVisibility(View.GONE);
                if(result.items.data.size() != 0)
                    content_layout.setVisibility(View.VISIBLE);
                else
                    nodata_layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(EventsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        clearAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                API.getApiInterface(EventsActivity.this).clearAllEvents(api_key, Lang.getCurrentLanguage(), new Callback<ApiInterface.ClearEventsResult>() {
                    @Override
                    public void success(ApiInterface.ClearEventsResult clearEventsResult, Response response) {
                        adapter.clear();
                        content_layout.setVisibility(View.GONE);
                        nodata_layout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(EventsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(EventsActivity.this);
                input.setSingleLine(true);
                if(searchtext != null) {
                    input.setText(searchtext);
                    input.setSelection(searchtext.length());
                }
                new AlertDialog.Builder(EventsActivity.this)
                        .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                searchtext = input.getText().toString();
                                adapter.getFilter().filter(searchtext);
                            }
                        })
                        .setView(input, Utils.dpToPx(EventsActivity.this, 10), 0, Utils.dpToPx(EventsActivity.this, 10), 0)
                        .setTitle(R.string.inputSearch)
                        .setNegativeButton(R.string.cancel, null)
                        .show();
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
