package com.example.yehoon.mbtaapp;

import android.content.res.Resources;
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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleFragment extends Fragment {
    private ArrayList<String> departureTimes, arrivalTimes;
    private RecyclerView recyclerView;
    private TextView noTimesFound;
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
        departureTimes = new ArrayList<>();
        arrivalTimes = new ArrayList<>();
        recyclerViewAdapter = new TimeAdapter(departureTimes, arrivalTimes);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initView(View rootView) {
        // show if no times found
        noTimesFound = (TextView) rootView.findViewById(R.id.tv_no_times_found);
        noTimesFound.setText("Select the start and end stops to see the train times.");
        if(recyclerViewAdapter.getItemCount() == 0)
            noTimesFound.setVisibility(View.VISIBLE);
    }

    private void updateRecyclerView() {
        if(departureTimes.size() != arrivalTimes.size()) { // must have even numbers for
            // recycler view display
            while(departureTimes.size() > arrivalTimes.size()) {
                departureTimes.remove(departureTimes.size() - 1);
            }
            while(departureTimes.size() < arrivalTimes.size()) {
                arrivalTimes.remove(arrivalTimes.size() - 1);
            }
        }
        recyclerViewAdapter = new TimeAdapter(departureTimes, arrivalTimes);
        recyclerView.setAdapter(recyclerViewAdapter);
        Log.d("LUKE", "departureTimes: " + departureTimes.size());
        Log.d("LUKE", "recyclerViewAdapter: " + recyclerViewAdapter.getItemCount());
        if (departureTimes.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noTimesFound.setVisibility(View.VISIBLE);
            Log.d("LUKE", "noTimesFound set to VISIBLE");
        } else {
            noTimesFound.setVisibility(View.GONE);
            Log.d("LUKE", "noTimesFound set to GONE");
        }
    }

    public void updateTimes(String transitID, String startStopID, String endStopID, String directionID) {
        Calendar calendar = Calendar.getInstance();
        String currentTime = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        int hours = Integer.parseInt(currentTime.substring(0, 2));

        Log.d("LUKE", "current time: " + currentTime);
        Log.d("LUKE", "current date: " + currentDate);
        Log.d("LUKE", "hours: " + hours);

        if(0 <= hours && hours < 2) {
            hours += 24;
            String minutes = currentTime.substring(2, 5);
            currentTime = Integer.toString(hours) + minutes;

            Log.d("LUKE", "minutes: " + minutes);
            Log.d("LUKE", "current time updated: " + currentTime);
            /*
            calendar.add(Calendar.DATE, -1);
            currentDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            Log.d("LUKE", "current date changed: " + currentDate);
            */
        }

        fetchDepartures(currentDate, directionID, "09:00", transitID, startStopID, endStopID);
        //new FetchSchedules().execute(currentDate, directionID, currentTime, transitID, startStopID, "depart");
        //new FetchSchedules().execute(currentDate, directionID, currentTime, transitID, endStopID, "arrive");
    }

    private void fetchDepartures(String currentDate, String directionID, String currentTime,
                                 String transitID, String startStopID, String endStopID) {
        new FetchSchedules().execute(currentDate, directionID, currentTime, transitID, startStopID,
                "depart", endStopID);
    }

    private void fetchArrivals(String currentDate, String directionID, String currentTIme,
                               String transitID, String endStopID, String endStopID2) { // for parameter consistency
        new FetchSchedules().execute(currentDate, directionID, currentTIme, transitID, endStopID,
                "arrive", endStopID2);
    }

    private class FetchSchedules extends AsyncTask<Object, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Object... params) {
            String date = (String) params[0];
            String directionID = (String) params[1];
            String minTime = (String) params[2];
            String route = (String) params[3];
            String stopID = (String) params[4];
            String isDepartStop = (String) params[5];
            String endStopID = (String) params[6];

            int hour = Integer.parseInt(minTime.substring(0, 2)) + 2;
            String maxTime = Integer.toString(hour) + minTime.substring(2, 5);
            Log.d("LUKE", "max time: " + maxTime);

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
                        .appendQueryParameter(STOP, stopID)
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
                    route, endStopID, isDepartStop};
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
            boolean isDepartStop = s[6].equals("depart") ? true : false;

            Log.d("LUKE", "isDepartStop: " + s[6]);

            try {
                // Convert the response into a JSON object.
                JSONObject jsonObject = new JSONObject(routeJSONString); //get top level object
                // Get the JSONArray of book items.
                JSONArray itemsArray = jsonObject.getJSONArray("data"); // array of times

                String timeRequestType;
                if(isDepartStop)
                    timeRequestType = "departure_time";
                else
                    timeRequestType = "arrival_time";
                ArrayList<String> times1 = new ArrayList<>(), times2 = new ArrayList<>();
                int stopSequence1 = 0, stopSequence2 = 0;
                for(int i = 0; i < itemsArray.length(); i++) {
                    // Get the current item information.
                    JSONObject dataObject = itemsArray.getJSONObject(i);
                    JSONObject attributeObject = dataObject.getJSONObject("attributes");

                    String time = attributeObject.getString(timeRequestType);
                    int stopSequence = attributeObject.getInt("stop_sequence");
                    if(stopSequence1 == 0) {
                        stopSequence1 = stopSequence;
                        Log.d("LUKE", "stopSequence1 = " + stopSequence1);
                    }
                    else if(stopSequence2 == 0) {
                        stopSequence2 = stopSequence;
                        Log.d("LUKE", "stopSequence2 = " + stopSequence2);
                    }

                    if(stopSequence == stopSequence1) {
                        times1.add(time.substring(11, 16)); // departureTimes.add(time.substring(11,16));
                        Log.d("LUKE", "adding time: " + time + " to times1");
                    }
                    else if(stopSequence == stopSequence2) {
                        times2.add(time.substring(11, 16));// arrivalTimes.add(time.substring(11,16));
                        Log.d("LUKE", "adding time: " + time + " to times2");

                    }
                }
                ArrayList<String> times = new ArrayList<>();
                Log.d("LUKE", "Heading " + directionID);
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
                if(isDepartStop) {
                    Log.d("LUKE", "assigning times to departureTimes");
                    departureTimes = times;
                    fetchArrivals(currentDate, directionID, currentTime, transitID, endStopID, endStopID);
                }
                else {
                    Log.d("LUKE", "assigning times to arrivalTimes");
                    arrivalTimes = times;
                    updateRecyclerView();
                }
            } catch (Exception e){
                // If onPostExecute does not receive a proper JSON string,
                // update the UI to show failed results.
                e.printStackTrace();
            }
        }
    }
}


