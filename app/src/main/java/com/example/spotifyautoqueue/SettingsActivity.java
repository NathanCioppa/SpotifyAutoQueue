package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

// Generic activity for stuff that could be considered settings I guess
public class SettingsActivity extends AppCompatActivity {

    // Stuff for building URL to retrieve authorization code
    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    final String SCOPE = "user-read-currently-playing user-read-playback-state";
    // The man, the myth, the legend, the URL
    final String AUTH_URL = "https://accounts.spotify.com/authorize?client_id="
            +CLIENT_ID+"&response_type=code&redirect_uri="
            +REDIRECT_URI+"&scope="
            +SCOPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void openAuthLink(View button) {
        Uri authPage = Uri.parse(AUTH_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, authPage);

        try {
            startActivity(intent);
        } catch (Error error) {
            ErrorLogActivity.logError("Error opening Auth link","Could not redirect to URL: "+AUTH_URL);
        }
    }

    public void submitAuthCode(View button) {
        ApiTokens.authCode = ((TextView) findViewById(R.id.inputAuthCode)).getText().toString();
        GetApiAccessTokens getApiAccessTokens = new GetApiAccessTokens();
        try{
            boolean result = getApiAccessTokens.execute().get();
            // Save the tokens if the auth code if they are successfully returned
            if (result)
                saveTokens();
            else
                ErrorLogActivity.logError("Failed to save tokens","Access tokens received from the Spotify API failed to save locally to the device. You may need to submit a new auth code.");

        } catch (Exception error) {
            error.printStackTrace();
            ErrorLogActivity.logError("Failed to submit auth code",error+"");
        }
    }

    public void saveTokens() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }

    public void resetAccessTokens(View button) {
        ApiTokens.authCode = "";
        ApiTokens.refreshToken = "";
        ApiTokens.accessToken = "";

        saveTokens(); // Save the tokens as being empty
    }

    public void openErrorLog(View button) {
        Intent errorLog = new Intent(this, ErrorLogActivity.class);
        startActivity(errorLog);
    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}