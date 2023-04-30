package com.example.spotifyautoqueue;

import android.app.Application;
import android.content.Intent;


import java.io.File;

// Just used to preform actions that should only be preformed once when the app is first started, or completely restarted
public class ThisApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Gets the tokens when the app is first started
        File externalDir = getExternalFilesDir(null);
        File file = new File(externalDir, "tokens.txt");
        ApiTokens.getTokens(file);

        // Starts the SpotifyService foreground service when the app is first started
        Intent intent = new Intent(this, SpotifyService.class);
        startService(intent);
    }
}
