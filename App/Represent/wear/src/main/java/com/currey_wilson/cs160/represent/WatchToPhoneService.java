package com.currey_wilson.cs160.represent;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/1/16.
 */


public class WatchToPhoneService extends Service {

    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String MESSAGE;
        final String MESSAGE_NAME;
        Bundle bundle = intent.getExtras();
        if (bundle.getString("R_LOCATION") != null) {
            // sending zipcode from shake
            MESSAGE_NAME = "/location";
            MESSAGE = bundle.getString("R_LOCATION");
        }
        else {
            ArrayList<String> repsData = bundle.getStringArrayList("REPS_DATA");
            int currentRep = bundle.getInt("CURRENT_REP");
            String using_zipcode = bundle.getBoolean("USE_ZIPCODE") ? "USE_ZIPCODE_TRUE" : "USE_ZIPCODE_FALSE";
            String location = bundle.getString("LOCATION");
            String theMessage = String.valueOf(currentRep) + "#" + using_zipcode + "#" + location + "~";
            for (String s : repsData) {
                theMessage += s + "!";
            }
            MESSAGE_NAME = "/reps";
            MESSAGE = theMessage;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //first, connect to the apiclient
                mApiClient.connect();
                sendMessage(MESSAGE_NAME, MESSAGE);
            }
        }).start();

        return START_STICKY;
    }

    @Override //remember, all services need to implement an IBiner
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage( final String path, final String text ) {
        //one way to send message: start a new thread and call .await()
        //see watchtophoneservice for another way to send a message
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    //we find 'nodes', which are nearby bluetooth devices (aka emulators)
                    //send a message for each of these nodes (just one, for an emulator)
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                    //4 arguments: api client, the node ID, the path (for the listener to parse),
                    //and the message itself (you need to convert it to bytes.)
                }
            }
        }).start();
    }

}