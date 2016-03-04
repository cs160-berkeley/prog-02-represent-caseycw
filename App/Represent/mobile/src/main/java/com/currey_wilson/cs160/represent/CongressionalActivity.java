package com.currey_wilson.cs160.represent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class CongressionalActivity extends AppCompatActivity {

    LinearLayout repList;
    LinearLayout entry1, entry2, entry3;
    ArrayList<String> repsData;
    TextView locationText;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);

        repList = (LinearLayout) findViewById(R.id.list);
        backButton = (Button) findViewById(R.id.congressional_back);

        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();
        boolean using_zip = bundle.getBoolean("USE_ZIPCODE", true);
        String zip = "[NO ZIPCODE]";
        String loc = "[NO LOCATION]";
        if (using_zip) {
            zip = bundle.getString("ZIPCODE", "[NO ZIPCODE]");
        }
        else {
            loc = bundle.getString("LOCATION", "[NO LOCATION]");
        }

        String zip_or_location = using_zip ? zip : loc;

        locationText = (TextView) repList.getChildAt(0);

        locationText.setText("Representatives near " + zip_or_location);

        entry1 = (LinearLayout) repList.getChildAt(1);
        entry2 = (LinearLayout) repList.getChildAt(2);
        entry3 = (LinearLayout) repList.getChildAt(3);

        updateData(zip_or_location);

        sendToWatch(0);

        // will need to modify in next phase
        setEntryListener(entry1, 0);
        setEntryListener(entry2, 1);
        setEntryListener(entry3, 2);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent backIntent = new Intent(CongressionalActivity.this, MainActivity.class);
            startActivity(backIntent);
            }
        });


        /*for (int i = 1; i < numEntries; i++) { //start loop after location text bar
            entry = (LinearLayout) repList.getChildAt(i);
            if (i < numEntries - 2) {
                next_entry = (LinearLayout) repList.getChildAt(i + 1);
            }
            else {
                next_entry = (LinearLayout) repList.getChildAt(1);
            }
            entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout theEntry = entry;
                    LinearLayout theNextEntry = next_entry;
                    Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                    TextView repName = (TextView) ((LinearLayout) theEntry.getChildAt(1)).getChildAt(0);
                    String name_text = repName.getText().toString();
                    sendIntent.putExtra("REP_NAME", name_text);
                    TextView party = (TextView) ((LinearLayout) theEntry.getChildAt(1)).getChildAt(1);
                    String party_text = party.getText().toString();
                    sendIntent.putExtra("REP_PARTY", party_text);
                    TextView next_name = (TextView) ((LinearLayout) theNextEntry.getChildAt(1)).getChildAt(0);
                    sendIntent.putExtra("NEXT_NAME", next_name.getText().toString());
                    TextView next_party = (TextView) ((LinearLayout) theNextEntry.getChildAt(1)).getChildAt(1);
                    sendIntent.putExtra("NEXT_PARTY", next_party.getText().toString());
                    startService(sendIntent);

                    Intent detailedIntent = new Intent(CongressionalActivity.this, DetailedActivity.class);
                    detailedIntent.putExtra("REP_NAME", name_text);
                    //System.out.println(name_text);
                    detailedIntent.putExtra("REP_PARTY", party_text);
                    startActivity(detailedIntent);
                }
            });
        }*/

    }

    private void setEntryListener(final LinearLayout entry, final int current_rep) {
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendToWatch(current_rep);

                Intent detailedIntent = new Intent(CongressionalActivity.this, DetailedActivity.class);
                detailedIntent.putStringArrayListExtra("REPS_DATA", repsData);
                detailedIntent.putExtra("CURRENT_REP", current_rep);
                startActivity(detailedIntent);
            }
        });
    }

    private void sendToWatch(final int current_rep) {
        Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
        sendIntent.putStringArrayListExtra("REPS_DATA", repsData);
        sendIntent.putExtra("CURRENT_REP", current_rep);
        startService(sendIntent);
    }

    // needs to be overwritten with APIs
    private void updateData(String location) {
        ArrayList<String> data = new ArrayList<String>();
        data.add("HILLARY CLINTON,DEMOCRATIC PARTY");
        data.add("MARCO RUBIO,REPUBLICAN PARTY");
        data.add("BERNIE SANDERS,INDEPENDENT PARTY");
        repsData = data;
    }

}
