package com.example.spotifyautoqueue;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class GetQueue extends AsyncTask<String, Void, ArrayList<String>> {

    String accessToken = ApiTokens.accessToken;

    @Override
    protected ArrayList<String> doInBackground(String... strings) {

        Log.d("GetQueue", "Called");

        try {
            // Create a URL object for the playback queue endpoint
            URL url = new URL("https://api.spotify.com/v1/me/player/queue");

            // Create a HttpURLConnection object and set the request method and headers
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Send the request and read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Parse the response body into a JSON object
                String responseBody = response.toString();
                JSONObject jsonObject = new JSONObject(responseBody);

                // Extract the track URIs from the JSON object
                //ArrayList<String> trackURIs = new ArrayList<>();
                JSONArray itemsArray = jsonObject.getJSONArray("queue");
                //for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemObject = itemsArray.getJSONObject(0);
                    String trackUri = (String) itemObject.getString("uri");

                    //String trackURI = trackObject.getString("uri");
                    //trackURIs.add(trackURI);
                //}

                System.out.println(trackUri);
                System.out.println(itemsArray.getJSONObject(0).getString("name"));
                //return trackURIs;
            } else {
                String errorMessage = "Error fetching playback queue: " + connection.getResponseMessage();
                Log.d("GetQueue | responseCode == HttpURLConnection.HTTP_OK", errorMessage+"");
            }
            connection.disconnect();
        } catch (JSONException | IOException e) {
            Log.d("GetQueue","ERROR");
        }

        return null;
    }
}
