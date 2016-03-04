package com.currey_wilson.cs160.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    TextView nameText;
    TextView partyText;
    Button voteButton;
    LinearLayout watchBackground;
    Random r;
    ArrayList<String> repsData;
    int currentRep;

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
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                nameText = (TextView) stub.findViewById(R.id.watch_name);
                partyText = (TextView) stub.findViewById(R.id.watch_party);
                watchBackground = (LinearLayout) stub.findViewById(R.id.watchBackground);
                voteButton = (Button) stub.findViewById(R.id.vote_button);

                Intent intentExtras = getIntent();
                Bundle bundle = intentExtras.getExtras();
                if (bundle != null) {
                    repsData = bundle.getStringArrayList("REPS_DATA");
                    currentRep = bundle.getInt("CURRENT_REP");
                    makeView();
                } else {
                    nameText.setText("REPRESENT!");
                    partyText.setText("Enter location on phone.");
                    voteButton.setEnabled(false);
                }

                voteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent voteIntent = new Intent(MainActivity.this, VoteActivity.class);
                        voteIntent.putStringArrayListExtra("REPS_DATA", repsData);
                        voteIntent.putExtra("CURRENT_REP", currentRep);
                        startActivity(voteIntent);
                    }
                });
            }
        });

        final GestureDetector gdt = new GestureDetector(new GestureListener());
        stub.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            currentRep += 1;
            if (currentRep == repsData.size()) {
                currentRep = 0;
            }
            Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneService.class);
            sendIntent.putStringArrayListExtra("REPS_DATA", repsData);
            sendIntent.putExtra("CURRENT_REP", currentRep);
            startService(sendIntent);

            makeView();
            return true;
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

    private void makeView() {
        String[] result = repsData.get(currentRep).split(",");

        nameText.setText(result[0]);
        partyText.setText(result[1]);
        voteButton.setEnabled(true);

        if (result[1].equals("DEMOCRATIC PARTY")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.democratBlue));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.democratBlue));
        } else if (result[1].equals("REPUBLICAN PARTY")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.republicanRed));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.republicanRed));
        } else if (result[1].equals("INDEPENDENT PARTY")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.independentYellow));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.independentYellow));
        }
    }
}
