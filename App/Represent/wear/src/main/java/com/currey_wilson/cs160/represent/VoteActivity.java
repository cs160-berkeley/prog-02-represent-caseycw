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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class VoteActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    TextView state;
    TextView county;
    TextView obama;
    TextView romney;
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
                state = (TextView) stub.findViewById(R.id.state);
                obama = (TextView) stub.findViewById(R.id.obama);
                romney = (TextView) stub.findViewById(R.id.romney);
                backButton = (Button) stub.findViewById(R.id.back_button);

                Intent intentExtras = getIntent();
                Bundle bundle = intentExtras.getExtras();
                final ArrayList<String> repsData = bundle.getStringArrayList("REPS_DATA");
                final int currentRep = bundle.getInt("CURRENT_REP");
                final String the_state = bundle.getString("STATE");
                final String the_county = bundle.getString("COUNTY");
                String[] result = repsData.get(currentRep).split(",");
                String name = result[0];
                state.setText(the_state);
                county.setText(the_county);

                try {
                    InputStream stream = getAssets().open("newelectioncounty2012.json");
                    int size = stream.available();
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    stream.close();
                    String jsonString = new String(buffer, "UTF-8");
                    JSONObject vote_data = new JSONObject(jsonString);
                    String query = the_county + ", " + the_state;
                    JSONObject percentages = vote_data.getJSONObject(query);
                    obama.setText("OBAMA: " + percentages.getInt("obama") + "%");
                    romney.setText("ROMNEY: " + percentages.getInt("romney") + "%");
                }
                catch (IOException e) {
                    Log.d("IO ERROR", e.getMessage());
                }
                catch (JSONException e) {
                    Log.d("JSON ERROR", e.getMessage());
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Intent backIntent = new Intent(VoteActivity.this, MainActivity.class);
                    backIntent.putStringArrayListExtra("REPS_DATA", repsData);
                    backIntent.putExtra("CURRENT_REP", currentRep);
                    backIntent.putExtra("STATE", the_state);
                    backIntent.putExtra("COUNTY", the_county);
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
        // generate latitudes within range of 33.98 -> 45.77, longitudes within range of -118.076 -> -89.07
        double random_lat = (r.nextDouble() * (45.77 - 33.98)) + 33.98;
        double random_long = (r.nextDouble() * (-89.07 - (-118.076))) - 118.076;

        Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
        sendIntent.putExtra("R_LOCATION", String.valueOf(random_lat) + "," + String.valueOf(random_long));
        startService(sendIntent);
    }
}
