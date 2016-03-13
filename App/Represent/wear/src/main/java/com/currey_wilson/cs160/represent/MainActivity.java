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

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

    private static final String TWITTER_KEY = "MCly8WvkJSefGI5O8aTD18uuh";
    private static final String TWITTER_SECRET = "r1eJKGLpaf9fQADILIT7T5iVD64Wnj11gn7YeTDJCfJuvAGXtU";


    private TextView mTextView;
    TextView nameText;
    TextView partyText;
    Button voteButton;
    LinearLayout watchBackground;
    Random r;
    ArrayList<String> repsData;
    int currentRep;
    String state;
    String county;
    boolean use_zipcode;
    String location;

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
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
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
                    state = bundle.getString("STATE");
                    county = bundle.getString("COUNTY");
                    use_zipcode = bundle.getBoolean("USE_ZIPCODE");
                    location = bundle.getString("LOCATION");
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
                        voteIntent.putExtra("STATE", state);
                        voteIntent.putExtra("COUNTY", county);
                        voteIntent.putExtra("USE_ZIPCODE", use_zipcode);
                        voteIntent.putExtra("LOCATION", location);
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
            sendIntent.putExtra("USE_ZIPCODE", use_zipcode);
            sendIntent.putExtra("LOCATION", location);
            startService(sendIntent);

            makeView();
            return true;
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

    private void makeView() {
        String[] result = repsData.get(currentRep).split(",");

        nameText.setText(result[0]);
        partyText.setText(result[1]);
        voteButton.setEnabled(true);

        if (result[1].equals("Democratic Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.democratBlue));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.democratBlue));
        } else if (result[1].equals("Republican Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.republicanRed));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.republicanRed));
        } else if (result[1].equals("Independent Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.independentYellow));
            watchBackground.setBackgroundColor(getResources().getColor(R.color.independentYellow));
        }
    }
}
