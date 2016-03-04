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
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages

    private static final String REPS_MESSAGE = "/reps";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, fred vs lexy)

        if( messageEvent.getPath().equalsIgnoreCase( REPS_MESSAGE ) ) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = value.split("~");
            int currentRep = Integer.valueOf(parts[0]);
            ArrayList<String> repsData = new ArrayList(Arrays.asList(parts[1].split("!")));
            Intent intent = new Intent(this, MainActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putStringArrayListExtra("REPS_DATA", repsData);
            intent.putExtra("CURRENT_REP", currentRep);
            Log.d("T", "about to start watch MainActivity rep info");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
