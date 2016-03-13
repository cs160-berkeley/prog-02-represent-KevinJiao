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
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Random;

public class Vote extends Activity implements SensorEventListener {
    SensorManager sensorMgr;
    Sensor sensor;
    float xaccel;
    float yaccel;
    float zaccel;
    LinearLayout romney;
    LinearLayout obama;
    TextView rText;
    TextView oText;


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (xaccel == 0.0f) {
            xaccel = event.values[0];
            yaccel = event.values[1];
            zaccel = event.values[2];
            return;
        }

        if ((xaccel != event.values[0] || yaccel != event.values[1] || zaccel != event.values[2])) {
            Random rand = new Random();
            int lat = rand.nextInt(148) + -124;
            int lon = rand.nextInt(116) + -66;
            xaccel = event.values[0];
            yaccel = event.values[1];
            zaccel = event.values[2];

            float Rvote = ((int) (rand.nextFloat() * 1000)) / 10;
            float Ovote = 100 - Rvote;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(romney.getLayoutParams());
            lp.weight = Rvote;
            romney.setLayoutParams(lp);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(obama.getLayoutParams());
            lp2.weight = Ovote;
            obama.setLayoutParams(lp2);
            String s = "Mitt Romney: " + Rvote + "%";
            rText.setText(s);
            String s2 = "Barack Obama: " + Ovote + "%";
            oText.setText(s2);
            Toast.makeText(this, "Showing results for location " + lat + ", " + lon,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);
        Intent intent = getIntent();
        romney = (LinearLayout) findViewById(R.id.romney);
        obama = (LinearLayout) findViewById(R.id.obama);
        rText = (TextView) findViewById(R.id.rtext);
        oText = (TextView) findViewById(R.id.otext);
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
