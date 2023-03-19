package com.example.spotifyautoqueue;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RefreshAccessToken extends AsyncTask<Void, Void, Boolean> {

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String CLIENT_SECRET = new SecretClass().CLIENT_SECRET;
    String refreshToken = ApiTokens.refreshToken;

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
                ApiTokens.accessToken = jsonObject.getString("access_token");

                Log.d("RefreshAccessToken", "Success");
            } else {
                System.out.println("Unexpected response code " + connection.getResponseCode());
                return false;
            }
            connection.disconnect();

        } catch (JSONException | IOException error) {
            ErrorLogActivity.logError("Error refreshing access token","Execution failed");
            error.printStackTrace();
            return false;
        }
        return true;
    }
}
