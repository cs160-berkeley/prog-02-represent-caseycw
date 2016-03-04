package com.currey_wilson.cs160.represent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;

public class DetailedActivity extends AppCompatActivity {

    TextView nameText;
    TextView partyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();
        ArrayList<String> repsData = bundle.getStringArrayList("REPS_DATA");
        int currentRep = bundle.getInt("CURRENT_REP");
        String[] result = repsData.get(currentRep).split(",");

        nameText = (TextView) findViewById(R.id.name);
        partyText = (TextView) findViewById(R.id.party);

        nameText.setText(result[0]);
        partyText.setText(result[1]);
    }

}
