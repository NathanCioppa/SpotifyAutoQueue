package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;

    SpotifyAppRemote spotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.disconnect(spotifyAppRemote);
        Log.d("onStart", "Disconnected");

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote sam) {
                spotifyAppRemote = sam;
                Log.d("MainActivity", "Connected");

                connected();
            }
            @Override
            public void onFailure(Throwable error) {
                Log.e("MainActivity", error.getMessage(), error);
                ErrorLogActivity.logError("Failed Spotify app remote connection",error.toString());
            }
        });
    }

    static String current = "unchanged";
    static String currentImageUrl = "";

    //ok so apparently this function is already called in the background basically whenever a new song plays anyway
    //which is fucking amazing for me.
    private void connected() {

        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            final Track track = playerState.track;
            if (track != null) {
                Log.d("MainActivity", track.name + " by " + track.artist.name);
                current = track.name+"";
                assert track.imageUri.raw != null;
                currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);
                System.out.println(currentImageUrl);

                updateWidget();

            } else {
                Log.d("MainActivity", "No track is playing");
                current = "No track is playing";
            }
            getNextInQueue();
        });
    }

    public void getNextInQueue() {
        try {
            GetQueue getQueue = new GetQueue();
            boolean getQueueResponse = getQueue.execute().get();
            if (!getQueueResponse) {
                ErrorLogActivity.logError("Error getting queue","Access token may be invalid, requesting new access token");
                RefreshAccessToken refreshAccessToken = new RefreshAccessToken();
                boolean refreshAccessResponse = refreshAccessToken.execute().get();

                if(refreshAccessResponse) {
                    saveTokens();
                    GetQueue secondGetQueue = new GetQueue();
                    boolean secondGetQueueResponse = secondGetQueue.execute().get();

                    if(!secondGetQueueResponse) {
                        Log.d("MainActivity | getNextInQueue", "error getting next in queue");
                        ErrorLogActivity.logError("Error getting queue", "Unable to retrieve playback queue from Spotify API after requesting new access token");
                    }
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            ErrorLogActivity.logError("Error getting next track in queue",e+"");
            e.printStackTrace();
        }
    }

    public void updateWidget() {
        Context appContext = this.getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int widgetId = R.layout.playback_widget;

        PlaybackWidget.updateAppWidget(appContext, appWidgetManager, widgetId);
    }




    public void saveTokens() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }

    public void openSettingsActivity(View button) {
        Intent openSettings = new Intent(this,SettingsActivity.class);
        startActivity(openSettings);
    }

    public void openCreateGroupActivity(View button) {
        Intent openCreateGroup = new Intent(this, CreateGroupActivity.class);
        startActivity(openCreateGroup);
    }
}