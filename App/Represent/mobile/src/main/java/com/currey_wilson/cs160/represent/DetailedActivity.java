package com.currey_wilson.cs160.represent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailedActivity extends AppCompatActivity {

    final String SUNLIGHT_COMMITTEES_BASE = "http://congress.api.sunlightfoundation.com/committees";
    final String SUNLIGHT_BILLS_BASE = "http://congress.api.sunlightfoundation.com/bills";
    final String IMAGE_BASE = "https://theunitedstates.io/images/congress/225x275/";

    final String SUNLIGHT_API_KEY = "c729dab0d2404a2fa988dc58cd9f67de";

    private static final String DEBUG_TAG = "HttpRequest";

    TextView nameText;
    TextView partyText;
    TextView termEndText;
    ImageView picture;
    TextView committees;
    TextView bills;
    Button backButton;
    boolean use_zipcode;
    String zip_or_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        Intent intentExtras = getIntent();
        Bundle bundle = intentExtras.getExtras();
        ArrayList<String> repsData = bundle.getStringArrayList("REPS_DATA");
        int currentRep = bundle.getInt("CURRENT_REP");
        use_zipcode = bundle.getBoolean("USE_ZIPCODE");
        zip_or_location = bundle.getString("LOCATION");
        final String[] result = repsData.get(currentRep).split(",");

        nameText = (TextView) findViewById(R.id.name);
        partyText = (TextView) findViewById(R.id.party);
        termEndText = (TextView) findViewById(R.id.term);
        picture = (ImageView) findViewById(R.id.picture);
        committees = (TextView) findViewById(R.id.committees);
        bills = (TextView) findViewById(R.id.bills);
        backButton = (Button) findViewById(R.id.detailed_back);

        // format: ("NAME[0],PARTY[1],ID[2],TWITTER_ID[3],CHAMBER[4],EMAIL[5],WEBSITE[6],TERM_END[7]")

        nameText.setText(result[0]);
        partyText.setText(result[1]);
        termEndText.setText("Term ends " + result[7]);


        new AsyncTask<Void, Void, Void>() {

            Bitmap bmp;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = new URL(IMAGE_BASE + result[2] + ".jpg").openStream();
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

        updateCommittees(result[2]);
        updateBills(result[2]);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(DetailedActivity.this, CongressionalActivity.class);
                backIntent.putExtra("USE_ZIPCODE", use_zipcode);
                backIntent.putExtra("LOCATION", zip_or_location);
                startActivity(backIntent);
            }
        });
    }

    private void updateCommittees(String id) {
        String url = SUNLIGHT_COMMITTEES_BASE + "?subcommittee=false&member_ids=" + id + "&apikey=" + SUNLIGHT_API_KEY;
        getFromURL(url, true);
    }

    private void updateBills(String id) {
        String url = SUNLIGHT_BILLS_BASE + "?sponsor_id=" + id + "&apikey=" + SUNLIGHT_API_KEY;
        getFromURL(url, false);
    }

    private void finishUpdatingCommittees(String urlResult) {
        String committeesText = "";
        //Toast.makeText(getApplicationContext(), "size: " + urlResult.length(), Toast.LENGTH_SHORT).show();

        try {
            JSONObject object = new JSONObject(urlResult);
            JSONArray results = object.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject committeeObject = results.getJSONObject(i);
                committeesText += (committeeObject.getString("name") + "\n");
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, e.getMessage());
        }

        committees.setText(committeesText);

    }

    private void finishUpdatingBills(String urlResult) {
        String billsText = "";
        //Toast.makeText(getApplicationContext(), "size: " + urlResult.length(), Toast.LENGTH_SHORT).show();

        try {
            JSONObject object = new JSONObject(urlResult);
            JSONArray results = object.getJSONArray("results");

            for (int i = 0; i < Math.min(results.length(), 5); i++) {
                JSONObject billObject = results.getJSONObject(i);
                String introduced_date = billObject.getString("introduced_on");
                String bill_name = billObject.getString("short_title");
                if (bill_name.equals("null")) bill_name = billObject.getString("official_title");
                billsText += (introduced_date + ": " + bill_name + "\n");
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, e.getMessage());
        }

        bills.setText(billsText);

    }

    private class DownloadCommitteesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            finishUpdatingCommittees(result);
        }
    }

    private class DownloadBillsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            finishUpdatingBills(result);
        }
    }

    private void getFromURL(String url, boolean isCommittees) {
        //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Toast.makeText(getApplicationContext(), "Network connection", Toast.LENGTH_SHORT).show();
            if (isCommittees) {
                new DownloadCommitteesTask().execute(url);
            }
            else {
                new DownloadBillsTask().execute(url);
            }
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
