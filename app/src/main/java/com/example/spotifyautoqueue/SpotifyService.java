package com.example.spotifyautoqueue;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class SpotifyService extends Service {
    final String TAG = "SpotifyService";

    private class BackgroundRemoteConnection extends AsyncTask<Void, Void, Void> {

        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override
        protected Void doInBackground(Void... voids) {

            spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                ErrorLogActivity.logError("appRemote callback", "callback event triggered");
                final Track track = playerState.track;

                if (track != null) {
                    ErrorLogActivity.logError("appRemote callback","playing: "+track.name);
                    currentName = track.name+"";
                    currentArtist = track.artist.name;

                    assert track.imageUri.raw != null;
                    currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);

                    updateWidget();
                    handler.postDelayed(SpotifyService.this::updateWidget, 1000);

                } else {
                    currentName = "No track is playing";
                }
                getNextInQueue();
            });

            return null;
        }
    }

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    SpotifyAppRemote spotifyAppRemote;

    private MusicPlayReceiver receiver;

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Started");



        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.disconnect(spotifyAppRemote);
        Log.d(TAG, "Disconnected");

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote sam) {
                spotifyAppRemote = sam;
                Log.d(TAG, "Connected");

                BackgroundRemoteConnection remoteConnection = new BackgroundRemoteConnection();
                remoteConnection.execute();
            }
            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, error.getMessage(), error);
                ErrorLogActivity.logError("Failed Spotify app remote connection",error.toString());
            }
        });

        receiver = new MusicPlayReceiver();
        IntentFilter filter = new IntentFilter("com.spotify.music.playbackstatechanged");
        Log.d(TAG, "onStartCommand: "+intent);
        registerReceiver(receiver, filter);

        return START_STICKY;
    }

    static String currentName = "";
    static String currentArtist ="";
    static String currentImageUrl="";

    public void updateWidget() {
        Context appContext = this.getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int widgetId = R.layout.playback_widget;

        PlaybackWidget.updateAppWidget(appContext, appWidgetManager, widgetId);
        Log.d(TAG,"updateWidget finished execution");
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
                        Log.d(TAG, "error getting next in queue");
                        ErrorLogActivity.logError("Error getting queue", "Unable to retrieve playback queue from Spotify API after requesting new access token");
                    }
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            ErrorLogActivity.logError("Error getting next track in queue",e+"");
            e.printStackTrace();
        }
    }

    public void saveTokens() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        ErrorLogActivity.logError("SpotifyService destroyed","Service class for handling app remote has stopped");
        Log.d(TAG,"destroyed");
    }
}
