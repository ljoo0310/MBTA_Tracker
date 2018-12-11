package com.example.yehoon.mbtaapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PredictionFragment extends Fragment {
    private ArrayList<Time> departureTimes, arrivalTimes;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tv_noTimesFound, tv_progress;
    private TimeAdapter recyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_prediction, container, false);

        setupRecyclerView(rootView);
        initView(rootView);

        return rootView;
    }

    private void setupRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new TimeAdapter(new ArrayList<String>(), new ArrayList<String>());
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initView(View rootView) {
        tv_progress = (TextView) rootView.findViewById(R.id.tv_progress);
        tv_progress.setVisibility(View.GONE);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        tv_progress.setVisibility(View.GONE);
        // show if no times found
        tv_noTimesFound = (TextView) rootView.findViewById(R.id.tv_no_times_found);
        tv_noTimesFound.setText("Select start and end stops to see the train times.");
        if(recyclerViewAdapter.getItemCount() == 0)
            tv_noTimesFound.setVisibility(View.VISIBLE);
    }

    private void updateRecyclerView() {
        ArrayList<String> adapterDepartureTimes = new ArrayList<>();
        ArrayList<String> adapterArrivalTimes = new ArrayList<>();
        for(int i = 0; i < departureTimes.size(); i++) {
            adapterDepartureTimes.add(departureTimes.get(i).getTime());
            adapterArrivalTimes.add(arrivalTimes.get(i).getTime());
        }
        recyclerViewAdapter = new TimeAdapter(adapterDepartureTimes, adapterArrivalTimes);
        recyclerView.setAdapter(recyclerViewAdapter);
        if (departureTimes.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tv_noTimesFound.setText("There are no prediction times available at the moment.");
            tv_noTimesFound.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            tv_noTimesFound.setVisibility(View.GONE);
        }

    }

    public void updateTimes(String transitID, String startStopID, String endStopID, String directionID) {
        if(transitID.equals("Choose") || startStopID.equals("Choose") || endStopID.equals("Choose")) {
            tv_noTimesFound.setText("Select start and end stops to see the train times.");
            tv_noTimesFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            tv_noTimesFound.setVisibility(View.GONE);
            String direction = directionID.equals("Eastbound") ? "1" : "0"; // Eastbound(inbound) = 1, Westbound(outbound) = 0;
            fetchDepartures(direction, startStopID, transitID, endStopID);
        }
    }

    private void fetchDepartures(String direction, String startStopID, String transitID, String endStopID) {
        new FetchDepartures().execute(direction, startStopID, transitID, endStopID);
    }

    private void fetchArrivals(String direction, String transitID, String endStopID) {
        new FetchArrivals().execute(direction, endStopID, transitID);
    }

    private class FetchDepartures extends AsyncTask<Object, Integer, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_progress.setText("Loading departure times...");
            tv_progress.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }

        @Override
        protected String[] doInBackground(Object... params) {
            String directionID = (String) params[0];
            String stop = (String) params[1];
            String route = (String) params[2];
            String endStopID = (String) params[3];

            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;

            try {
                final String START_BASE_URL = "https://api-v3.mbta.com/predictions?";
                final String API_KEY = "api_key";
                final String SORT = "sort";
                final String STOP = "filter[stop]";
                final String ROUTE = "filter[route]";

                // Build up your query URI, limiting results to 10 items and printed movies.
                Uri builtURI = Uri.parse(START_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                        .appendQueryParameter(SORT, "arrival_time")
                        .appendQueryParameter(STOP, stop)
                        .appendQueryParameter(ROUTE, route)
                        .build();

                URL requestURL = new URL(builtURI.toString());
                Log.d("LUKE", "PredictionFragment: FetchDepartures - URL = " + builtURI.toString());

                // Open the network connection.
                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Get the InputStream.
                InputStream inputStream = urlConnection.getInputStream();

                // Read the response string into a StringBuilder.
                StringBuilder builder = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // but it does make debugging a *lot* easier if you print out the completed buffer for debugging.
                    builder.append(line + "\n");
                    publishProgress();
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    // return null;
                    return null;
                }
                routeJSONString = builder.toString();
            } catch (IOException e) { // Catch errors.
                e.printStackTrace();
            } finally { // Close the connections.
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            String[] boolJSONPair = new String[]{routeJSONString, directionID, route, endStopID};
            return boolJSONPair;
        }

        @Override
        protected void onPostExecute(String[] s) {
            String routeJSONString = s[0];
            String directionID = s[1];
            String transitID = s[2];
            String endStopID = s[3];

            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of times

                ArrayList<Time> times = new ArrayList<>();
                for(int i = 0; i < itemsArray.length() && times.size() < 15; i++) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(i);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    JSONObject relationshipsObject = dataObject.getJSONObject("relationships");
                    JSONObject tripObject = relationshipsObject.getJSONObject("trip");
                    JSONObject tripDataObject = tripObject.getJSONObject("data");

                    String directionID_recv = Integer.toString(attributeObject.getInt("direction_id"));
                    String tripID = tripDataObject.getString("id");
                    if(tripID.length() > 8)
                        tripID = tripID.substring(0, 8);

                    if(directionID.equals(directionID_recv)) {
                        String str_time = attributeObject.getString("departure_time");
                        if(str_time.length() < 5)
                            str_time = attributeObject.getString("arrival_time");
                        str_time = str_time.substring(11, 16);
                        Time time = new Time();
                        time.setTIme(str_time);
                        time.setTripID(tripID);
                        times.add(time);
                    }
                }
                departureTimes = times;
                tv_progress.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                fetchArrivals(directionID, transitID, endStopID);
            } catch (Exception e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                e.printStackTrace();
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.incrementProgressBy(10);
        }
    }

    private class FetchArrivals extends AsyncTask<Object, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_progress.setText("Loading arrival times...");
            tv_progress.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(Object... params) {
            String directionID = (String) params[0];
            String stop = (String) params[1];
            String route = (String) params[2];

            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;

            try {
                final String START_BASE_URL = "https://api-v3.mbta.com/predictions?";
                final String API_KEY = "api_key";
                final String SORT = "sort";
                final String STOP = "filter[stop]";
                final String ROUTE = "filter[route]";

                // Build up your query URI, limiting results to 10 items and printed movies.
                Uri builtURI = Uri.parse(START_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                        .appendQueryParameter(SORT, "arrival_time")
                        .appendQueryParameter(STOP, stop)
                        .appendQueryParameter(ROUTE, route)
                        .build();

                URL requestURL = new URL(builtURI.toString());
                Log.d("LUKE", "PredictionFragment: FetchArrivals - URL = " + builtURI.toString());

                // Open the network connection.
                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Get the InputStream.
                InputStream inputStream = urlConnection.getInputStream();

                // Read the response string into a StringBuilder.
                StringBuilder builder = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // but it does make debugging a *lot* easier if you print out the completed buffer for debugging.
                    builder.append(line + "\n");
                    publishProgress();
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    // return null;
                    return null;
                }
                routeJSONString = builder.toString();
            } catch (IOException e) { // Catch errors.
                e.printStackTrace();
            } finally { // Close the connections.
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return routeJSONString;
        }

        @Override
        protected void onPostExecute(String s) {
            String routeJSONString = s;

            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of times
                arrivalTimes = new ArrayList<>();
                int index = 0;
                while(arrivalTimes.size() < departureTimes.size()) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(index);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    JSONObject relationshipsObject = dataObject.getJSONObject("relationships");
                    JSONObject tripObject = relationshipsObject.getJSONObject("trip");
                    JSONObject tripDataObject = tripObject.getJSONObject("data");

                    String tripID = tripDataObject.getString("id");
                    if(tripID.length() > 8)
                        tripID = tripID.substring(0, 8);

                    for(int i = 0; i < departureTimes.size(); i++) {
                        if(tripID.equals(departureTimes.get(i).getTripID())) {
                            String str_time = attributeObject.getString("arrival_time").substring(11,16);
                            Time time = new Time();
                            time.setTIme(str_time);
                            time.setTripID(tripID);
                            arrivalTimes.add(time);
                            break;
                        }
                    }
                    index++;
                }
                tv_progress.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                updateRecyclerView();
            } catch (Exception e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.incrementProgressBy(10);
        }
    }
}


