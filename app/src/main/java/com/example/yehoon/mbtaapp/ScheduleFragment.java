package com.example.yehoon.mbtaapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleFragment extends Fragment {
    private ArrayList<Time> departureTimes, arrivalTimes;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tv_noTimesFound, tv_progress;
    private TimeAdapter recyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_schedule, container, false);

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
        tv_noTimesFound.setText("Select the start and end stops to see the train times.");
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
            tv_noTimesFound.setVisibility(View.VISIBLE);
        } else
            tv_noTimesFound.setVisibility(View.GONE);
    }

    public void updateTimes(String transitID, String startStopID, String endStopID, String directionID) {
        Calendar calendar = Calendar.getInstance();
        String currentTime = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        int hours = Integer.parseInt(currentTime.substring(0, 2));

        if(0 <= hours && hours < 2) {
            hours += 24;
            String minutes = currentTime.substring(2, 5);
            currentTime = Integer.toString(hours) + minutes;
            /*
            calendar.add(Calendar.DATE, -1);
            currentDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            */
        }

        tv_noTimesFound.setVisibility(View.GONE);
        fetchDepartures(currentDate, directionID, "09:00", transitID, startStopID, endStopID);
        //fetchDepartures(currentDate, directionID, currentTime, transitID, startStopID, endStopID);
    }

    private void fetchDepartures(String currentDate, String directionID, String currentTime,
                                 String transitID, String startStopID, String endStopID) {
        new FetchDepartures().execute(currentDate, directionID, currentTime, transitID, startStopID,
                endStopID);
    }

    private void fetchArrivals(String currentDate, String directionID, String currentTIme,
                               String transitID, String endStopID, String endStopID2) { // for parameter consistency
        new FetchArrivals().execute(currentDate, directionID, currentTIme, transitID, endStopID,
                "arrive", endStopID2);
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
            String date = (String) params[0];
            String directionID = (String) params[1];
            String minTime = (String) params[2];
            String route = (String) params[3];
            String startStopID = (String) params[4];
            String endStopID = (String) params[5];

            int hour = Integer.parseInt(minTime.substring(0, 2)) + 1;
            String maxTime = Integer.toString(hour) + minTime.substring(2, 5);

            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;

            try {
                final String START_BASE_URL = "https://api-v3.mbta.com/schedules?";
                final String API_KEY = "api_key";
                final String SORT = "sort";
                final String DATE = "filter[date]";
                final String DIRECTION = "filter[direction_id]";
                final String MIN_TIME = "filter[min_time]";
                final String MAX_TIME = "filter[max_time]";
                final String ROUTE = "filter[route]";
                final String STOP = "filter[stop]";

                // Build up your query URI, limiting results to 10 items and printed movies.
                Uri builtURI = Uri.parse(START_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                        .appendQueryParameter(SORT, "arrival_time")
                        .appendQueryParameter(DATE, date)
                        .appendQueryParameter(DIRECTION, directionID)
                        .appendQueryParameter(MIN_TIME, minTime)
                        .appendQueryParameter(MAX_TIME, maxTime)
                        .appendQueryParameter(ROUTE, route)
                        .appendQueryParameter(STOP, startStopID)
                        .build();

                URL requestURL = new URL(builtURI.toString());

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

            String[] boolJSONPair = new String[]{routeJSONString, date, directionID, minTime,
                    route, endStopID};
            return boolJSONPair;
        }

        @Override
        protected void onPostExecute(String[] s) {
            String routeJSONString = s[0];
            String currentDate = s[1];
            String directionID = s[2];
            String currentTime = s[3];
            String transitID = s[4];
            String endStopID = s[5];

            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of times

                ArrayList<Time> times1 = new ArrayList<>(), times2 = new ArrayList<>();
                int stopSequence1 = 0, stopSequence2 = 0;
                for(int i = 0; i < itemsArray.length(); i++) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(i);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    JSONObject relationshipsObject = dataObject.getJSONObject("relationships");
                    JSONObject tripObject = relationshipsObject.getJSONObject("trip");
                    JSONObject tripDataObject = tripObject.getJSONObject("data");

                    String str_time = attributeObject.getString("departure_time");
                    String tripID = tripDataObject.getString("id");
                    int stopSequence = attributeObject.getInt("stop_sequence");
                    if(stopSequence1 == 0) {
                        stopSequence1 = stopSequence;
                    }
                    else if(stopSequence2 == 0) {
                        stopSequence2 = stopSequence;
                    }
                    Time time = new Time();
                    time.setTIme(str_time.substring(11, 16));
                    time.setTripID(tripID);

                    if(stopSequence == stopSequence1)
                        times1.add(time);
                    else if(stopSequence == stopSequence2)
                        times2.add(time);
                }
                ArrayList<Time> times = new ArrayList<>();
                if(directionID.equals("Eastbound")) {
                    if(stopSequence1 < stopSequence2)
                        times = times1;
                    else
                        times = times2;
                }
                else if(directionID.equals("Westbound")) {
                    if(stopSequence1 < stopSequence2)
                        times = times2;
                    else
                        times = times1;
                }
                departureTimes = times;
                tv_progress.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                fetchArrivals(currentDate, directionID, currentTime, transitID, endStopID, endStopID);
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

    private class FetchArrivals extends AsyncTask<Object, Integer, String[]> {
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
        protected String[] doInBackground(Object... params) {
            String date = (String) params[0];
            String directionID = (String) params[1];
            String minTime = (String) params[2];
            String route = (String) params[3];
            String endStopID = (String) params[4];

            int hour = Integer.parseInt(minTime.substring(0, 2)) + 2;
            String maxTime = Integer.toString(hour) + minTime.substring(2, 5);

            // Set up variables for the try block that need to be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String routeJSONString = null;

            try {
                final String START_BASE_URL = "https://api-v3.mbta.com/schedules?";
                final String API_KEY = "api_key";
                final String SORT = "sort";
                final String DATE = "filter[date]";
                final String DIRECTION = "filter[direction_id]";
                final String MIN_TIME = "filter[min_time]";
                final String MAX_TIME = "filter[max_time]";
                final String ROUTE = "filter[route]";
                final String STOP = "filter[stop]";

                // Build up your query URI, limiting results to 10 items and printed movies.
                Uri builtURI = Uri.parse(START_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "0dc3ff9c85fb41a9b6af4ffa33572593")
                        .appendQueryParameter(SORT, "arrival_time")
                        .appendQueryParameter(DATE, date)
                        .appendQueryParameter(DIRECTION, directionID)
                        .appendQueryParameter(MIN_TIME, minTime)
                        .appendQueryParameter(MAX_TIME, maxTime)
                        .appendQueryParameter(ROUTE, route)
                        .appendQueryParameter(STOP, endStopID)
                        .build();

                URL requestURL = new URL(builtURI.toString());

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

            String[] boolJSONPair = new String[]{routeJSONString, date, directionID, minTime,
                    route, endStopID};
            return boolJSONPair;
        }

        @Override
        protected void onPostExecute(String[] s) {
            String routeJSONString = s[0];

            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of times

                arrivalTimes = new ArrayList<>();
                int index = 0, departureTimeCounter = 0;
                while(departureTimeCounter < departureTimes.size()) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(index);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");
                    JSONObject relationshipsObject = dataObject.getJSONObject("relationships");
                    JSONObject tripObject = relationshipsObject.getJSONObject("trip");
                    JSONObject tripDataObject = tripObject.getJSONObject("data");

                    String str_time = attributeObject.getString("arrival_time");
                    String tripID = tripDataObject.getString("id");

                    if(tripID.equals(departureTimes.get(departureTimeCounter).getTripID())) {
                        Time time = new Time();
                        time.setTIme(str_time.substring(11, 16));
                        time.setTripID(tripID);
                        arrivalTimes.add(time);
                        departureTimeCounter++;
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


