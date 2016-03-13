package com.currey_wilson.cs160.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.JsonReader;

import com.google.gson.JsonObject;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// much of the following comes from http://developer.android.com/intl/es/training/basics/network-ops/connecting.html#http-client
// and http://developer.android.com/reference/android/util/JsonReader.html

public class CongressionalActivity extends AppCompatActivity {

    final String SUNLIGHT_LEGISLATORS_BASE = "http://congress.api.sunlightfoundation.com/legislators/locate";
    final String GEOCODE_BASE = "https://maps.googleapis.com/maps/api/geocode/json";
    final String IMAGE_BASE = "https://theunitedstates.io/images/congress/225x275/";

    final String SUNLIGHT_API_KEY = "c729dab0d2404a2fa988dc58cd9f67de";
    final String GOOGLE_API_KEY = "AIzaSyDbfXjdcw5wvxaxy8b3L5zvCiBjiLZDlSU";

    Context appContext;

    private static final String DEBUG_TAG = "HttpRequest";

    Geocoder geocoder;

    LinearLayout repList;
    LinearLayout entry1, entry2, entry3;
    ArrayList<String> repsData;
    TextView locationText;
    Button backButton;
    String state = "DEFAULT_STATE";
    String county = "DEFAULT_COUNTY";
    boolean using_zip;
    String zip_or_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);

        if (getIntent().getExtras() != null) {
            appContext = getApplicationContext();

            geocoder = new Geocoder(getApplicationContext());

            repList = (LinearLayout) findViewById(R.id.list);
            backButton = (Button) findViewById(R.id.congressional_back);

            Intent intentExtras = getIntent();
            Bundle bundle = intentExtras.getExtras();
            using_zip = bundle.getBoolean("USE_ZIPCODE", true);

            zip_or_location = bundle.getString("LOCATION", "[NO LOCATION]");

            locationText = (TextView) findViewById(R.id.location);

            locationText.setText("Representatives near " + zip_or_location);

            entry1 = (LinearLayout) repList.getChildAt(0);
            entry2 = (LinearLayout) repList.getChildAt(1);
            entry3 = (LinearLayout) repList.getChildAt(2);

            getStateCountyFromLocation(zip_or_location, using_zip);

            //Toast.makeText(getApplicationContext(), using_zip ? "USING_ZIP" : "NOT USING ZIP", Toast.LENGTH_SHORT).show();
            //Toast.makeText(getApplicationContext(), zip_or_location, Toast.LENGTH_SHORT).show();

            updateData(zip_or_location, using_zip);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent backIntent = new Intent(CongressionalActivity.this, MainActivity.class);
                    startActivity(backIntent);
                }
            });
        }
    }

    private void setEntryListener(final LinearLayout entry, final int current_rep) {
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                sendToWatch(current_rep);

                Intent detailedIntent = new Intent(CongressionalActivity.this, DetailedActivity.class);
                detailedIntent.putStringArrayListExtra("REPS_DATA", repsData);
                detailedIntent.putExtra("CURRENT_REP", current_rep);
                detailedIntent.putExtra("STATE", state);
                detailedIntent.putExtra("COUNTY", county);
                detailedIntent.putExtra("USE_ZIPCODE", using_zip);
                detailedIntent.putExtra("LOCATION", zip_or_location);
                startActivity(detailedIntent);
            }
        });
    }

    private void sendToWatch(final int current_rep) {
        Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
        sendIntent.putStringArrayListExtra("REPS_DATA", repsData);
        sendIntent.putExtra("CURRENT_REP", current_rep);
        sendIntent.putExtra("STATE", state);
        sendIntent.putExtra("COUNTY", county);
        sendIntent.putExtra("USE_ZIPCODE", using_zip);
        sendIntent.putExtra("LOCATION", zip_or_location);
        startService(sendIntent);
    }

    private void updateData(String location, boolean is_zipcode) {
        String url;
        if (is_zipcode) {
            url = SUNLIGHT_LEGISLATORS_BASE + "?zip=" + location + "&apikey=" + SUNLIGHT_API_KEY;
        } else {
            String[] latlong = location.split(",");
            url = SUNLIGHT_LEGISLATORS_BASE + "?latitude=" + latlong[0] + "&longitude=" + latlong[1] + "&apikey=" + SUNLIGHT_API_KEY;
        }
        getFromURL(url, true);
    }

    private void finishUpdatingData(String urlResult) {
        ArrayList<String> data = new ArrayList<String>();
        //Toast.makeText(getApplicationContext(), "size: " + urlResult.length(), Toast.LENGTH_SHORT).show();

        try {
            JSONObject object = new JSONObject(urlResult);
            JSONArray results = object.getJSONArray("results");

            String[] info = new String[8];
            String first_name = "", last_name = "";
            for (int i = 0; i < results.length(); i++) {
                JSONObject repObject = results.getJSONObject(i);
                first_name = repObject.getString("first_name");
                last_name = repObject.getString("last_name");
                String party_abbr = repObject.getString("party");
                info[0] = first_name + " " + last_name;
                info[1] = party_abbr.equals("D") ? "Democratic Party" : (party_abbr.equals("R") ? "Republican Party" : "Independent Party");
                info[2] = repObject.getString("bioguide_id");
                info[3] = repObject.getString("twitter_id");
                info[4] = repObject.getString("chamber");
                info[5] = repObject.getString("oc_email");
                info[6] = repObject.getString("website");
                info[7] = repObject.getString("term_end");
                String line = "";
                for (String s : info) {
                    line += (s + ",");
                }
                data.add(line);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, e.getMessage());
        }
        // format: ("NAME[0],PARTY[1],ID[2],TWITTER_ID[3],CHAMBER[4],EMAIL[5],WEBSITE[6],TERM_END[7]")
        // TEMP DATA
        /*
        data.add("HILLARY CLINTON,DEMOCRATIC PARTY");
        data.add("MARCO RUBIO,REPUBLICAN PARTY");
        data.add("BERNIE SANDERS,INDEPENDENT PARTY");
        */
        repsData = data;


        if (repsData.size() == 3) {
            //Toast.makeText(getApplicationContext(), "SIZE IS 3", Toast.LENGTH_SHORT).show();
            updateRep(entry1, 0);
            updateRep(entry2, 1);
            updateRep(entry3, 2);
        }
        else if (repsData.size() > 3) {
            //Toast.makeText(getApplicationContext(), "SIZE IS " + String.valueOf(repsData.size()), Toast.LENGTH_SHORT).show();
            updateRep(entry1, 0);
            updateRep(entry2, 1);
            updateRep(entry3, 2);
            LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int i = 3; i < repsData.size(); i++) {
                LinearLayout newChild = (LinearLayout) li.inflate(R.layout.rep_template, null);
                updateRep(newChild, i);
                repList.addView(newChild);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "COULDN'T GET REPS", Toast.LENGTH_SHORT).show();
        }

        sendToWatch(0);
    }

    private void getStateCountyFromLocation(String loc, boolean is_zip) {
        String latitude;
        String longitude;


        if (!is_zip) {
            String[] latlong = loc.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        } else {
            try {
                List<Address> locations = geocoder.getFromLocationName(loc, 1);
                Address location = locations.get(0);
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());

            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "FAILURE TO CONVERT FROM ZIP", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String url = GEOCODE_BASE + "?latlng=" + latitude + "," + longitude
                + "&result_type=administrative_area_level_1|administrative_area_level_2&key=" + GOOGLE_API_KEY;
        getFromURL(url, false);
    }

    private String getStateCountyFromURL(String urlResult) {
        try {
            JSONObject object = new JSONObject(urlResult);
            JSONArray results = object.getJSONArray("results");
            JSONObject firstLocation = results.getJSONObject(0);
            JSONArray address_components = firstLocation.getJSONArray("address_components");
            JSONObject countyObject = address_components.getJSONObject(0);
            county = countyObject.getString("long_name");
            JSONObject stateObject = address_components.getJSONObject(1);
            state = stateObject.getString("short_name");
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, e.getMessage());
        }

        return state + "," + county;
    }

    // format: ("NAME[0],PARTY[1],ID[2],TWITTER_ID[3],CHAMBER[4],EMAIL[5],WEBSITE[6]")
    private void updateRep(LinearLayout rep, int id) {
        final String[] repItems = repsData.get(id).split(",");
        final ImageView picture = (ImageView) rep.getChildAt(0);
        ScrollView container = (ScrollView) rep.getChildAt(1);
        final LinearLayout factList = (LinearLayout) container.getChildAt(0);
        TextView repName = (TextView) factList.getChildAt(0);
        TextView repParty = (TextView) factList.getChildAt(1);
        TextView repEmail = (TextView) factList.getChildAt(2);
        TextView repWebsite = (TextView) factList.getChildAt(3);

        repName.setText(repItems[0]);
        repParty.setText(repItems[1]);
        repEmail.setText(repItems[5]);
        repWebsite.setText(repItems[6]);

        if (repItems[1].equals("Democratic Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.democratBlue));
            rep.setBackgroundColor(getResources().getColor(R.color.democratBlue));
        } else if (repItems[1].equals("Republican Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.republicanRed));
            rep.setBackgroundColor(getResources().getColor(R.color.republicanRed));
        } else if (repItems[1].equals("Independent Party")) {
            //watchBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.independentYellow));
            rep.setBackgroundColor(getResources().getColor(R.color.independentYellow));
        }

        new AsyncTask<Void, Void, Void>() {

            Bitmap bmp;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = new URL(IMAGE_BASE + repItems[2] + ".jpg").openStream();
                    bmp = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    // log error
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (bmp != null)
                    picture.setImageBitmap(bmp);
            }

        }.execute();

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        // Can also use Twitter directly: Twitter.getApiClient()
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.userTimeline(null, repItems[3], 1, null, null, false, false, false, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                TweetView tweetView = new TweetView(CongressionalActivity.this, result.data.get(0));
                factList.addView(tweetView);
            }

            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Load Tweet failure", exception);
            }
        });



        setEntryListener(rep, id);
    }

    private class DownloadSunlightTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //Toast.makeText(appContext, "tryna download url", Toast.LENGTH_SHORT).show();
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                //Toast.makeText(appContext, "IO Exception from URL", Toast.LENGTH_SHORT).show();
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            finishUpdatingData(result);
        }
    }

    private class DownloadGoogleTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //Toast.makeText(appContext, "tryna download url", Toast.LENGTH_SHORT).show();
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                //Toast.makeText(appContext, "IO Exception from URL", Toast.LENGTH_SHORT).show();
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            getStateCountyFromURL(result);
        }
    }

    private void getFromURL(String url, boolean use_sunlight) {
        //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Toast.makeText(getApplicationContext(), "Network connection", Toast.LENGTH_SHORT).show();
            if (use_sunlight) new DownloadSunlightTask().execute(url);
            else new DownloadGoogleTask().execute(url);
        } else {
            Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        Log.d(DEBUG_TAG, "TRYING GET REQUEST");
        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        int response = conn.getResponseCode();
        Log.d(DEBUG_TAG, "The response is: " + response);
        //Log.d(DEBUG_TAG, "Message: " + conn.getResponseMessage());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();
        String contentAsString = stringBuilder.toString();
        Log.d(DEBUG_TAG, contentAsString);
        return contentAsString;
    }
}
