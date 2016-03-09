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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.internal.IMapFragmentDelegate;
import com.google.android.gms.plus.internal.model.people.PersonEntity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.*;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Congressional extends AppCompatActivity {
    private ViewGroup mRelativeLayout;
    ArrayList<JSONObject> reps = new ArrayList<JSONObject>();
    ArrayAdapter<JSONObject> adapter;


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
                        reps.add(repObj);
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

    private class repAdapter extends ArrayAdapter<JSONObject> {
        Context context;
        List<JSONObject> objects;

        public repAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            JSONObject rep = objects.get(pos);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View repView = inflater.inflate(R.layout.rep_card, parent, false);
            TextView name = (TextView) repView.findViewById(R.id.name);
            TextView desc = (TextView) repView.findViewById(R.id.desc);
            ImageView home = (ImageView) repView.findViewById(R.id.home);
            ImageView email = (ImageView) repView.findViewById(R.id.email);
            ImageView twitter = (ImageView) repView.findViewById(R.id.twitter);
            ImageView info = (ImageView) repView.findViewById(R.id.info);

            try {
                StringBuilder sb = new StringBuilder();
                sb.append(rep.getString("title"))
                        .append(" ")
                        .append(rep.getString("first_name"))
                        .append(" ")
                        .append(rep.getString("last_name"))
                        .append(" ")
                        .append(rep.getString("party"));
                name.setText(sb.toString());
                desc.setText(rep.getString("website"));
                return repView;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
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