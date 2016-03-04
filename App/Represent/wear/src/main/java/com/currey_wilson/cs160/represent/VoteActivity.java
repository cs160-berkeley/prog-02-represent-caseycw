package com.currey_wilson.cs160.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

public class VoteActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    TextView state;
    TextView county;
    Random r;
    Button backButton;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean wasPreviouslyShaking;


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mTextView = (TextView) stub.findViewById(R.id.text);
                county = (TextView) stub.findViewById(R.id.county);
                backButton = (Button) stub.findViewById(R.id.back_button);

                Intent intentExtras = getIntent();
                Bundle bundle = intentExtras.getExtras();
                final ArrayList<String> repsData = bundle.getStringArrayList("REPS_DATA");
                final int currentRep = bundle.getInt("CURRENT_REP");
                String[] result = repsData.get(currentRep).split(",");
                String name = result[0];
                county.setText("NEAR " + name); // fill this in with API later

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Intent backIntent = new Intent(VoteActivity.this, MainActivity.class);
                    backIntent.putStringArrayListExtra("REPS_DATA", repsData);
                    backIntent.putExtra("CURRENT_REP", currentRep);
                    startActivity(backIntent);
                    }
                });
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        wasPreviouslyShaking = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            if (x > 10 || y > 10 || z > 10) {
                if (!wasPreviouslyShaking) {
                    wasPreviouslyShaking = true;
                    updateLocation();
                    //Toast.makeText(MainActivity.this, String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                wasPreviouslyShaking = false;
            }

        }
    }

    private void updateLocation() {
        r = new Random();
        String random_zip = String.valueOf(r.nextInt((99999-10000) + 1) + 10000);

        // Update other lines based on API

        Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
        sendIntent.putExtra("ZIPCODE", random_zip);
        startService(sendIntent);
    }
}
