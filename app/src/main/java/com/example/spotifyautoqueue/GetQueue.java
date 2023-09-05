package com.example.spotifyautoqueue;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class GetQueue extends AsyncTask<Void, Void, Boolean> {

    String accessToken = ApiTokens.accessToken;

    // Gets the user's current playback queue from the Spotify Web API
    // Returns true if the request is successful and sets the nextTrackUri in SpotifyService class
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
                JSONObject fullQueueResponse = new JSONObject(responseBody);

                JSONArray queueArray = fullQueueResponse.getJSONArray("queue");
                if(queueArray.length() == 0) {
                    ErrorLogActivity.logError("GetQueue","Retrieved an empty queue");
                    connection.disconnect();
                    return false;
                }

                String nextTrackUri = queueArray.getJSONObject(0).getString("uri");

                // Occasionally the queue would seemingly not be updated, so the same track would be queued repeatedly no matter which was playing or next
                // This should insure that a track is only queued if the playback queue is correctly retrieved.
                if(!Objects.equals(SpotifyService.nextTrackUri, nextTrackUri)) {
                    SpotifyService.currentTrackUri = fullQueueResponse.getJSONObject("currently_playing").getString("uri");
                    SpotifyService.nextTrackUri = nextTrackUri;
                }
                else {
                    connection.disconnect();
                    return null;
                }

            } else {
                ErrorLogActivity.logError("GetQueue","Bad response. CODE: "+responseCode+". "+"MESSAGE: "+connection.getResponseMessage()+".");
                connection.disconnect();
                return false;
            }
            connection.disconnect();

        } catch (Exception error) {
            ErrorLogActivity.logError("GetQueue","Full Exception: "+error.getMessage());
            return false;
        }
        return true;
    }
}
