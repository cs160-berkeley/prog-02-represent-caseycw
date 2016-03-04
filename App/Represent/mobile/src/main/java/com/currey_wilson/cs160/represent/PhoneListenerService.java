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
    private static final String ZIP_MESSAGE = "/zipcode";
    private static final String REPS_MESSAGE = "/reps";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Intent mainIntent = null;
        //mainIntent.getExtras(); // dumb code for testing
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase(ZIP_MESSAGE) ) {
            // Value contains the String we sent over in WatchToPhoneService, "good job"
            String zipcode = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, CongressionalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("ZIPCODE", zipcode);
            intent.putExtra("USE_ZIPCODE", true);
            startActivity(intent);

            // Make a toast with the String
            //Context context = getApplicationContext();
            //int duration = Toast.LENGTH_SHORT;

            //Toast toast = Toast.makeText(context, value, duration);
            //toast.show();

            // so you may notice this crashes the phone because it's
            //''sending message to a Handler on a dead thread''... that's okay. but don't do this.
            // replace sending a toast with, like, starting a new activity or something.
            // who said skeleton code is untouchable? #breakCSconceptions
        } else if (messageEvent.getPath().equalsIgnoreCase(REPS_MESSAGE)) {

            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = value.split("~");
            int currentRep = Integer.valueOf(parts[0]);
            ArrayList<String> repsData = new ArrayList<String>(Arrays.asList(parts[1].split("!")));
            Intent intent = new Intent(this, DetailedActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putStringArrayListExtra("REPS_DATA", repsData);
            intent.putExtra("CURRENT_REP", currentRep);
            Log.d("T", "about to start phone DetailedActivity rep info");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}
