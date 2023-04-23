package com.example.spotifyautoqueue;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;

public class SearchSpotify extends AsyncTask<Void, Void, ArrayList<SearchItem>> {

    String accessToken = ApiTokens.accessToken;
    String searchQuery;

    @Override
    protected ArrayList<SearchItem> doInBackground(Void... voids) {
        if(searchQuery == null || searchQuery.equals("")) {
            ErrorLogActivity.logError("Error searching Spotify","attempted a null or empty search");
            return null;
        }


        String endpoint = "https://api.spotify.com/v1/search?q=" + searchQuery + "&type=track";

        try{
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject tracks;
                try{
                    tracks = new JSONObject(jsonResponse.getJSONObject("tracks").toString());
                } catch (Exception e) {
                    ErrorLogActivity.logError("Error searching Spotify","no \"tracks\" item found in response. Full response: "+jsonResponse);
                    return null;
                }

                JSONArray items = new JSONArray(tracks.getJSONArray("items").toString());

                ArrayList<SearchItem> searches = new ArrayList<>();
                for(int i = 0; i < items.length(); i++) {
                    JSONObject trackInfo = new JSONObject(items.get(i).toString());

                    String name = trackInfo.getString("name");
                    String artist = new JSONObject(trackInfo.getJSONArray("artists").get(0).toString()).getString("name");
                    String imageUrl = new JSONObject(trackInfo.getJSONObject("album").getJSONArray("images").get(0).toString()).getString("url");
                    String uri = trackInfo.getString("uri");

                    SearchItem track = new SearchItem(name,artist,imageUrl,uri);
                    searches.add(track);
                }
                return searches;

            } else {
                ErrorLogActivity.logError("Error searching Spotify","Bad HTTP request. RESPONSE CODE: "+connection.getResponseCode()+". RESPONSE MESSAGE: "+connection.getResponseMessage());
                return null;
            }

        } catch (JSONException | IOException e) {
            ErrorLogActivity.logError("Error searching Spotify",e+"");
            e.printStackTrace();
            return null;
        }
    }
}
