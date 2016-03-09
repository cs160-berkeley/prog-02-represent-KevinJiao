package com.kevin.represent;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.*;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Congressional extends AppCompatActivity {
    private ViewGroup mRelativeLayout;
    ArrayList<String> reps = new ArrayList<String>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);
        mRelativeLayout = (ViewGroup) findViewById(R.id.rep_layout);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://congress.api.sunlightfoundation.com/legislators/locate", this.getParams(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                JSONObject res;
                try {
                    res = new JSONObject(new String(response));
                    JSONArray repObjects = res.getJSONArray("results");
                    for (int i = 0; i < res.getInt("count"); i++) {
                        JSONObject repObj = repObjects.getJSONObject(i);
                        String name = repObj.getString("first_name") + " " + repObj.getString("last_name");
                        reps.add(name);
                    }
                    System.out.println(reps);
                    adapter.notifyDataSetChanged();
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("SUNLIGHT API FAILED");
                System.out.println(new String(responseBody));
            }
        });
        adapter = new repAdapter(this, R.layout.rep_card, reps);
        ListView repList = (ListView) findViewById(R.id.rep_list);
        repList.setAdapter(adapter);
    }

    public void getDetails(View view) {
        Intent intent = new Intent(this, Detail.class);
        startActivity(intent);
    }

    private class repAdapter extends ArrayAdapter<String> {
        Context context;
        List<String> objects;

        public repAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View repView = inflater.inflate(R.layout.rep_card, parent, false);
            TextView name = (TextView) repView.findViewById(R.id.name);
            name.setText(objects.get(pos));
            return repView;
        }
    }

    private RequestParams getParams() {
        RequestParams params = new RequestParams();
        Intent intent = getIntent();
        String zip;
        double lat, lon;
        params.put("apikey", "a347f6559ad649bf940fae34119fe3c4");
        if (intent.hasExtra("ZIP")) {
            zip = intent.getStringExtra("ZIP");
            params.put("zip", zip);
            System.out.println("Using ZIP: " + zip);
        } else if (intent.hasExtra("LAT")) {
            lat = intent.getDoubleExtra("LAT", 0);
            lon = intent.getDoubleExtra("LON", 0);
            params.put("latitude", lat);
            params.put("longitude", lon);
            System.out.println("Using lat + long " + lat + ", " + lon);
        } else {
            System.out.println("NO LOCATION DATA IN INTENT");
        }
        return params;
    }
}