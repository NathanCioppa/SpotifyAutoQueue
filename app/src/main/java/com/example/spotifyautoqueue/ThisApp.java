package com.example.spotifyautoqueue;

import android.app.Application;

import java.io.File;

public class ThisApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("APP LOADED");

        File externalDir = getExternalFilesDir(null);
        File file = new File(externalDir, "tokens.txt");
        ApiTokens.getTokens(file);

        new RefreshAccessToken().execute();
    }
}
