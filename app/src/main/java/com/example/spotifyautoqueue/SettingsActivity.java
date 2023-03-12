package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;


public class SettingsActivity extends AppCompatActivity {

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    final String SCOPE = "user-read-currently-playing user-read-playback-state";

    final String AUTH_URL = "https://accounts.spotify.com/authorize?client_id="
            +CLIENT_ID+"&response_type=code&redirect_uri="
            +REDIRECT_URI+"&scope="
            +SCOPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void openAuthLink(View button){
        Uri authPage = Uri.parse(AUTH_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, authPage);

        try {
            startActivity(intent);
        }
        catch (Error error) {
            System.out.println(error);
        }
    }

    public void submitAuthCode(View button) {
        ApiTokens.authCode = ((TextView) findViewById(R.id.inputAuthCode)).getText().toString();
        GetApiAccessTokens getApiAccessTokens = new GetApiAccessTokens();
        getApiAccessTokens.execute();

    }

    public void saveTokens(View button) {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }

    public void backToHome(View button) {
        System.out.println("BACK TO HOME");
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}