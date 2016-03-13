package com.kevin.represent;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main extends WearableActivity implements SensorEventListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "itvCAJ5eEsRZqAlCkIfg0x0mn";
    private static final String TWITTER_SECRET = "2gymT2JmnBC2cpRzzD37oSFmjVTsy17xrDTy0s01W8SD30RlAi";

    SensorManager sensorMgr;
    Sensor sensor;
    boolean notSet = false;
    float xaccel;
    float yaccel;
    float zaccel;
    ArrayList<JSONObject> reps = new ArrayList<>();
    ArrayAdapter<JSONObject> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
        TextView districtView = (TextView) findViewById(R.id.district);
        Intent intent = this.getIntent();
        String repsJSON = "";
        if (intent.hasExtra("REPS")) {
            repsJSON = intent.getStringExtra("REPS");
            System.out.println(repsJSON);
        }
        JSONObject res;
        try {
            res = new JSONObject(repsJSON);
            JSONArray repObjects = res.getJSONArray("results");
            for (int i = 0; i < res.getInt("count"); i++) {
                JSONObject repObj = repObjects.getJSONObject(i);
                reps.add(repObj);
            }
            reps.add(res.getJSONObject("countyresults"));
            String description = "Representatives for " + res.getString("county");
            districtView.setText(description);
        } catch (org.json.JSONException e) {
            System.out.println("Error parsing reps JSON String");
            e.printStackTrace();
        }
        adapter = new repAdapter(this, R.layout.rep_card, reps);
        ListView repList = (ListView) findViewById(R.id.rep_cards);
        repList.setAdapter(adapter);
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = this.getIntent();
        if (intent.hasExtra("ZIP")) {
            String zip = intent.getExtras().getString("ZIP");
            TextView header = (TextView) findViewById(R.id.header);
            String text = "Representatives for ZIP " + zip;
            header.setText(text);
        } else if (intent.hasExtra("LAT")) {
            Double lat = intent.getDoubleExtra("LAT", 0);
            Double lon = intent.getDoubleExtra("LON", 0);
            System.out.println("Using lat + long " + lat + ", " + lon);
            TextView header = (TextView) findViewById(R.id.header);
            String text = "Representatives for lat: " + lat + ", lon: " + lon;
            header.setText(text);
        }
    }


    public void viewDetailed(View view) {
        Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
        startService(sendIntent);
    }

    public void viewVote(View view) {
        Intent sendIntent = new Intent(getBaseContext(), Vote.class);
        startActivity(sendIntent);
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
            final JSONObject rep = objects.get(pos);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View repCard = inflater.inflate(R.layout.rep_card, parent, false);
            TextView nameView = (TextView) repCard.findViewById(R.id.title);
            if (pos == objects.size() - 1) {
                nameView.setText("Click to see 2012 election results for this county");
                final String results = objects.get(pos).toString();
                repCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent sendIntent = new Intent(getBaseContext(), Vote.class);
                        sendIntent.putExtra("results", results);
                        startActivity(sendIntent);
                    }
                });
                return repCard;
            }
            String name = "blank";
            try {
                name = rep.getString("first_name") + " " + rep.getString("last_name") + " " + rep.getString("party");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nameView.setText(name);
            repCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
                    sendIntent.putExtra("JSON", rep.toString());
                    startService(sendIntent);
                }
            });
            return repCard;
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
        }
        return params;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (notSet) {
            xaccel = event.values[0];
            yaccel = event.values[1];
            zaccel = event.values[2];
            notSet = false;
            return;
        }

        if ((xaccel != event.values[0] || yaccel != event.values[1] || zaccel != event.values[2])) {
            Random rand = new Random();
            int lat = rand.nextInt(148) + -124;
            int lon = rand.nextInt(116) + -66;
            xaccel = event.values[0];
            yaccel = event.values[1];
            zaccel = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
