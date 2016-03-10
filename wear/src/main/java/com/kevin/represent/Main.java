package com.kevin.represent;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import java.util.Random;


public class Main extends WearableActivity implements SensorEventListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "itvCAJ5eEsRZqAlCkIfg0x0mn";
    private static final String TWITTER_SECRET = "2gymT2JmnBC2cpRzzD37oSFmjVTsy17xrDTy0s01W8SD30RlAi";

    private static final int SHAKE_THRESHOLD = 800;
    SensorManager sensorMgr;
    Sensor sensor;
    boolean notSet = false;
    float xaccel;
    float yaccel;
    float zaccel;

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
            Toast toast = Toast.makeText(getApplicationContext(), "New lat: " + lat + "New long: " + lon, Toast.LENGTH_LONG);
            toast.show();
            xaccel = event.values[0];
            yaccel = event.values[1];
            zaccel = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
        Intent intent = this.getIntent();
        if (intent.getExtras() != null) {
            String zip = intent.getExtras().getString("ZIP");
            TextView header = (TextView) findViewById(R.id.header);
            String text = "Representatives for ZIP " + zip;
            header.setText(text);
        }

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = this.getIntent();
        if (intent.getExtras() != null) {
            String zip = intent.getExtras().getString("ZIP");
            TextView header = (TextView) findViewById(R.id.header);
            String text = "Representatives for ZIP " + zip;
            header.setText(text);
        }
    }


    public void viewDetailed(View view) {
        System.out.println("ViewDetailed");
        Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
        startService(sendIntent);
    }

    public void viewVote(View view) {
        Intent sendIntent = new Intent(getBaseContext(), Vote.class);
        startActivity(sendIntent);
    }
}
