package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    final String CLIENT_ID = "1435a3ff53c84332b17feb17f4cb0cb8";
    final String CLIENT_SECRET = new SecretClass().CLIENT_SECRET;
    final String REDIRECT_URI = "https://nathancioppa.github.io/spotify-app.html";
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
        String authCode = ((TextView) findViewById(R.id.inputAuthCode)).getText().toString();
        Log.d("submitAuthCode", authCode);
    }

    public void backToHome(View button) {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }
}