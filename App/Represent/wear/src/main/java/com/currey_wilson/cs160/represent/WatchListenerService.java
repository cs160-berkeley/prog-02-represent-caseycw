package com.currey_wilson.cs160.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Casey on 3/1/16.
 */
public class WatchListenerService extends WearableListenerService {

    private static final String REPS_MESSAGE = "/reps";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());


        if( messageEvent.getPath().equalsIgnoreCase( REPS_MESSAGE ) ) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = value.split("~");
            String[] initial_parts = parts[0].split("#");
            int currentRep = Integer.valueOf(initial_parts[0]);
            String state = initial_parts[1];
            String county = initial_parts[2];
            boolean use_zipcode = initial_parts[3].equals("USE_ZIPCODE_TRUE");
            String location = initial_parts[4];
            ArrayList<String> repsData = new ArrayList(Arrays.asList(parts[1].split("!")));
            Intent intent = new Intent(this, MainActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putStringArrayListExtra("REPS_DATA", repsData);
            intent.putExtra("CURRENT_REP", currentRep);
            intent.putExtra("STATE", state);
            intent.putExtra("COUNTY", county);
            intent.putExtra("USE_ZIPCODE", use_zipcode);
            intent.putExtra("LOCATION", location);
            Log.d("T", "about to start watch MainActivity rep info");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
