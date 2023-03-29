package com.example.spotifyautoqueue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.Objects;
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
        try {
            createNotificationChannel();
            Notification notification = buildNotification();
            startForeground(1, notification);

            filter = new IntentFilter("com.spotify.music.active");
            registerReceiver(spotifyReceiver, filter); //start to check for spotify to activate again

        } catch (Exception e) {
            ErrorLogActivity.logError("Error starting foreground service", e.toString());
            if(spotifyAppRemote != null && !spotifyAppRemote.isConnected()) {
                SpotifyAppRemote.disconnect(spotifyAppRemote);
            }
            unregisterReceiver(spotifyReceiver);
        }
    }

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    SpotifyAppRemote spotifyAppRemote;
    Context context = SpotifyService.this;
    IntentFilter filter;

    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();
        return START_STICKY;
    }

    public void connectRemote() {
        if (spotifyAppRemote != null && !spotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
        } // make sure that mf is disconnected

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote sam) {
                spotifyAppRemote = sam; //we love sam here :)
                startRemote();
            }

            @Override
            public void onFailure(Throwable error) {
                SpotifyAppRemote.disconnect(spotifyAppRemote);
                registerReceiver(spotifyReceiver, filter); //start to check for spotify to activate again

                ErrorLogActivity.logError("Failed Spotify app remote connection","disconnecting, waiting for spotify to be opened");
            }
        });
    }

    static String currentName = "";
    static String currentArtist ="";
    static String currentImageUrl="";

    public void startRemote() {

        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {

            final Track track = playerState.track;

            if (track != null) {
                currentName = track.name+"";
                currentArtist = track.artist.name;
                if (track.imageUri.raw != null)
                    currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);

                updateWidget();
            } else {
                currentName = "No track is playing";
            }

            //getNextInQueue();
            // not needed for app functionality yet
        });
    }

    public void updateWidget() {
        Context appContext = this.getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int widgetId = R.layout.playback_widget;

        PlaybackWidget.updateAppWidget(appContext, appWidgetManager, widgetId);
    }

    public void getNextInQueue() {
        try {
            GetQueue getQueue = new GetQueue();
            boolean getQueueResponse = getQueue.execute().get();
            if (!getQueueResponse) {
                RefreshAccessToken refreshAccessToken = new RefreshAccessToken();
                boolean refreshAccessResponse = refreshAccessToken.execute().get();

                if(refreshAccessResponse) {
                    saveTokens();
                    GetQueue secondGetQueue = new GetQueue();
                    secondGetQueue.execute();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            ErrorLogActivity.logError("Error getting next track in queue","Execution failed epicly >:)");
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

    private final BroadcastReceiver spotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ErrorLogActivity.logError("spotifyReceiver onReceive","Receiver detected spotify is active, reconnecting remote");
            connectRemote();
            unregisterReceiver(spotifyReceiver);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(spotifyReceiver);
        SpotifyAppRemote.disconnect(spotifyAppRemote);
        ErrorLogActivity.logError("SpotifyService destroyed","service for handling background connection has been terminated");
    }

}
