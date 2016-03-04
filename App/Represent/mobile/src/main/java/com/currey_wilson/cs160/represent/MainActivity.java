package com.currey_wilson.cs160.represent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText zipCode;
    Button mSearchButton;
    Button mCurrentLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchButton = (Button) findViewById(R.id.search);
        mCurrentLocationButton = (Button) findViewById(R.id.currentLocation);
        zipCode = (EditText) findViewById(R.id.zipCode);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                //sendIntent.putExtra("USE_ZIPCODE", true);
                String code = zipCode.getText().toString();
                //sendIntent.putExtra("ZIPCODE", code);
                //startService(sendIntent);
                Intent congressionalIntent = new Intent(MainActivity.this, CongressionalActivity.class);
                congressionalIntent.putExtra("USE_ZIPCODE", true);
                congressionalIntent.putExtra("ZIPCODE", code);
                startActivity(congressionalIntent);
            }
        });

        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                //sendIntent.putExtra("USE_ZIPCODE", false);
                String loc = "00000"; // overwrite later
                //sendIntent.putExtra("LOCATION", loc);
                //startService(sendIntent);
                Intent congressionalIntent = new Intent(MainActivity.this, CongressionalActivity.class);
                congressionalIntent.putExtra("USE_ZIPCODE", false);
                congressionalIntent.putExtra("LOCATION", loc);
                startActivity(congressionalIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
