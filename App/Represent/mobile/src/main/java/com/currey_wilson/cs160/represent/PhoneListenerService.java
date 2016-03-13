package com.currey_wilson.cs160.represent;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Casey on 3/1/16.
 */
public class PhoneListenerService extends WearableListenerService {

    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String ZIP_MESSAGE = "/location";
    private static final String REPS_MESSAGE = "/reps";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase(ZIP_MESSAGE) ) {
            String location = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, CongressionalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("LOCATION", location);
            intent.putExtra("USE_ZIPCODE", false);
            startActivity(intent);

        } else if (messageEvent.getPath().equalsIgnoreCase(REPS_MESSAGE)) {

            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = value.split("~");
            String[] initial_parts = parts[0].split("#");
            int currentRep = Integer.valueOf(initial_parts[0]);
            boolean use_zipcode = initial_parts[1].equals("USE_ZIPCODE_TRUE");
            String location = initial_parts[2];
            ArrayList<String> repsData = new ArrayList<String>(Arrays.asList(parts[1].split("!")));
            Intent intent = new Intent(this, DetailedActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putStringArrayListExtra("REPS_DATA", repsData);
            intent.putExtra("CURRENT_REP", currentRep);
            intent.putExtra("USE_ZIPCODE", use_zipcode);
            intent.putExtra("LOCATION", location);
            Log.d("T", "about to start phone DetailedActivity rep info");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
