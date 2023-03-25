package com.example.spotifyautoqueue;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class SpotifyService extends Service {
    final String TAG = "SpotifyService";
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "SpotifyService",
                    "AutoQueue",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        } else {
            ErrorLogActivity.logError("Unable to create notification channel","failed to pass `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)`");
        }
    }

    private Notification buildNotification() {
         NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SpotifyService")
                .setContentTitle("AutoQueue is running in the background")
                .setContentText("Tap to open the app")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setOngoing(true);

        return builder.build();
    }

    private void startForegroundService() {
        try{
            createNotificationChannel();
            Notification notification = buildNotification();
            startForeground(1, notification);
        } catch (Exception e){
            ErrorLogActivity.logError("Error starting foreground service", e.toString());
        }
    }

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    static SpotifyAppRemote spotifyAppRemote;

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Started");
        startForegroundService();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote sam) {
                spotifyAppRemote = sam;
                Log.d(TAG, "Connected");

                startRemote();
            }
            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, error.getMessage(), error);
                ErrorLogActivity.logError("Failed Spotify app remote connection",error.toString());
            }
        });

        return START_STICKY;
    }

    static String currentName = "";
    static String currentArtist ="";
    static String currentImageUrl="";

    public void startRemote() {
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            ErrorLogActivity.logError("appRemote callback", "callback event triggered");
            final Track track = playerState.track;

            if (track != null) {
                ErrorLogActivity.logError("appRemote callback","playing: "+track.name);
                currentName = track.name+"";
                currentArtist = track.artist.name;

                assert track.imageUri.raw != null;
                Log.d(TAG,track.imageUri.raw+"");
                currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);

                updateWidget();

            } else {
                currentName = "No track is playing";
            }
            getNextInQueue();
        });
    }

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
        Log.d(TAG,"destroyed");
    }
}
