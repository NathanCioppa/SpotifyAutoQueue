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
import java.util.Map;

public class SearchSpotify extends AsyncTask<Void, Void, Boolean> {

    String accessToken = ApiTokens.accessToken;
    String searchQuery;

    @Override
    protected Boolean doInBackground(Void... voids) {

        String endpoint = "https://api.spotify.com/v1/search?q=" + searchQuery + "&type=track";

        try{
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject tracks = new JSONObject(jsonResponse.getJSONObject("tracks").toString());
            JSONArray items = new JSONArray(tracks.getJSONArray("items").toString());

            for(int i = 0; i < items.length(); i++) {
                JSONObject track = new JSONObject(items.get(i).toString());
                String artist = new JSONObject(track.getJSONArray("artists").get(0).toString()).getString("name");
                System.out.println(artist);
            }
            Log.d("SearchSpotify", "Results: "+items);
        } catch (JSONException | IOException e) {
            Log.d("SearchSpotify", "ERROR: "+e);
            return false;
        }
        return true;
    }
}
