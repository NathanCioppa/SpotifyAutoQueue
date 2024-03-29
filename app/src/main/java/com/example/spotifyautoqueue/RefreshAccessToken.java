package com.example.spotifyautoqueue;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Class responsible for making HTTP request to refresh the access token
public class RefreshAccessToken extends AsyncTask<Void, Void, Boolean> {

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String CLIENT_SECRET = new SecretClass().CLIENT_SECRET;
    String refreshToken = ApiTokens.refreshToken;

    // Returns true if the request is successful, false otherwise and logs an error
    @Override
    protected Boolean doInBackground(Void... params) {

        try{
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(), Base64.NO_WRAP));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String requestBody = "grant_type=refresh_token&refresh_token=" + refreshToken;

            connection.setDoOutput(true);
            connection.getOutputStream().write(requestBody.getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject jsonObject = new JSONObject(response.toString());
                // Set the new access token the the ApiTokens class
                ApiTokens.accessToken = jsonObject.getString("access_token");
            } else {
                ErrorLogActivity.logError("RefreshAccessToken","BAD RESPONSE");
                return false;
            }

            connection.disconnect();

        } catch (JSONException | IOException error) {
            ErrorLogActivity.logError("Error refreshing access token",error+"");
            return false;
        }
        return true;
    }
}
