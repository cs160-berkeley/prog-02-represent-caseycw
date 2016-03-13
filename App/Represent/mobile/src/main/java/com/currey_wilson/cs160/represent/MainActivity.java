package com.currey_wilson.cs160.represent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TWITTER_KEY = "vnpXNGboG0BF7ruNiH1bCdFHG";
    private static final String TWITTER_SECRET = "10AKJadT6BOdKFQsnzkDC4hUYFl1BW9a4hgfuhP41JiomgPgja";

    private TwitterLoginButton loginButton;

    EditText zipCode;
    Button mSearchButton;
    Button mCurrentLocationButton;
    GoogleApiClient mGoogleApiClient;
    boolean accessingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mSearchButton = (Button) findViewById(R.id.search);
        mCurrentLocationButton = (Button) findViewById(R.id.currentLocation);
        zipCode = (EditText) findViewById(R.id.zipCode);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = zipCode.getText().toString();
                Intent congressionalIntent = new Intent(MainActivity.this, CongressionalActivity.class);
                congressionalIntent.putExtra("USE_ZIPCODE", true);
                congressionalIntent.putExtra("LOCATION", code);
                startActivity(congressionalIntent);
            }
        });

        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accessingLocation && ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (currentLocation != null) {
                        String loc = String.valueOf(currentLocation.getLatitude()) + "," + String.valueOf(currentLocation.getLongitude());
                        Intent congressionalIntent = new Intent(MainActivity.this, CongressionalActivity.class);
                        congressionalIntent.putExtra("USE_ZIPCODE", false);
                        congressionalIntent.putExtra("LOCATION", loc);
                        startActivity(congressionalIntent);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        accessingLocation = true;
        mCurrentLocationButton.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        accessingLocation = false;
        mCurrentLocationButton.setEnabled(false);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        accessingLocation = false;
        mCurrentLocationButton.setEnabled(false);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "location :" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}
