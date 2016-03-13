package com.kevin.represent;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class Vote extends Activity implements SensorEventListener {
    SensorManager sensorMgr;
    Sensor sensor;
    float xaccel;
    float yaccel;
    float zaccel;


    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);
        Intent intent = getIntent();
        LinearLayout romney = (LinearLayout) findViewById(R.id.romney);
        LinearLayout obama = (LinearLayout) findViewById(R.id.obama);
        TextView rText = (TextView) findViewById(R.id.rtext);
        TextView oText = (TextView) findViewById(R.id.otext);
        double Rvote, Ovote;
        try {
            JSONObject result = new JSONObject(intent.getStringExtra("results"));
            Rvote = result.getDouble("romney");
            Ovote = result.getDouble("obama");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(romney.getLayoutParams());
            lp.weight = (float) Rvote;
            romney.setLayoutParams(lp);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(obama.getLayoutParams());
            lp2.weight = (float) Ovote;
            obama.setLayoutParams(lp2);
            String s = "Mitt Romney: " + Rvote + "%";
            rText.setText(s);
            String s2 = "Barack Obama: " + Ovote + "%";
            oText.setText(s2);

        } catch (Exception e) {
            e.printStackTrace();
        }
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
