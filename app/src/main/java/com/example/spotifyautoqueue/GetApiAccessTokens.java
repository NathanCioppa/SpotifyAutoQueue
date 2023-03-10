package com.example.spotifyautoqueue;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class GetApiAccessTokens extends AsyncTask<String, Void, String> {

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String CLIENT_SECRET = new SecretClass().CLIENT_SECRET;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    String authCode = ApiTokens.authCode;

    @Override
    protected String doInBackground(String... strings) {

        try {
            String authString = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuthString = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
            }

            String data = "grant_type=authorization_code" +
                    "&code=" + URLEncoder.encode(authCode, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuthString);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(data);
            outputStream.flush();
            outputStream.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseString = response.toString();
            ApiTokens.refreshToken = responseString.substring(responseString.indexOf("refresh_token\":\"") + 16, responseString.indexOf("\",\"scope\""));
            ApiTokens.accessToken = responseString.substring(responseString.indexOf("access_token\":\"") + 15, responseString.indexOf("\",\"token_type\""));

        } catch (Error | UnsupportedEncodingException error) {
            Log.d("ERROR: CLASS ApiTokens, getRefreshAccessToken",error+"");
        } catch (IOException e) {
            Log.d("ERROR: CLASS ApiTokens, getRefreshAccessToken","Auth code invalid");
        }
        return null;
    }
}
