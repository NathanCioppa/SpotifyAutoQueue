package com.example.spotifyautoqueue;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.io.File;

public class ThisApp extends Application {

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;

    SpotifyAppRemote spotifyAppRemote;
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("APP LOADED");

        File externalDir = getExternalFilesDir(null);
        File file = new File(externalDir, "tokens.txt");
        ApiTokens.getTokens(file);

        Intent intent = new Intent(this, SpotifyService.class);
        startService(intent);

    }


}
