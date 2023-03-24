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
import java.net.URL;
import java.util.ArrayList;


public class GetQueue extends AsyncTask<Void, Void, Boolean> {

    String accessToken = ApiTokens.accessToken;

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            URL url = new URL("https://api.spotify.com/v1/me/player/queue");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String responseBody = response.toString();
                JSONObject jsonObject = new JSONObject(responseBody);

                JSONArray itemsArray = jsonObject.getJSONArray("queue");
                    JSONObject itemObject = itemsArray.getJSONObject(0);
                    String trackUri = itemObject.getString("uri");

                Log.d("GetQueue", "trackUri: "+trackUri);
                Log.d("GetQueue", "trackName: "+itemsArray.getJSONObject(0).getString("name"));

            } else {
                return false;
            }
            connection.disconnect();

        } catch (JSONException | IOException e) {
            return false;
        }
        return true;
    }
}
