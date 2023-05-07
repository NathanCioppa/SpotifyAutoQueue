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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

// Service runs in the background to activate groups and update the widget, basically everything important
public class SpotifyService extends Service {

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

            // Get the saved groups and set them to the groups ArrayList
            MainActivity.getGroups(this);

            // Register the receiver to receive broadcasts when Spotify starts playing a new track
            filter = new IntentFilter("com.spotify.music.active");
            registerReceiver(spotifyReceiver, filter);

        } catch (Exception e) {
            ErrorLogActivity.logError("Error starting foreground service", e.toString());
            if(spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                SpotifyAppRemote.disconnect(spotifyAppRemote);
            }
            unregisterReceiver(spotifyReceiver);
        }
    }

    static ArrayList<AutoqueueGroup> groups = new ArrayList<>(); // ArrayList containing all of the user's groups

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    SpotifyAppRemote spotifyAppRemote;
    Context context = SpotifyService.this;
    IntentFilter filter;

    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        // first 3 intents are for the widget
        if ("PLAY_NEXT".equals(action))
            playNext();
        else if ("TOGGLE_PAUSE".equals(action))
            togglePause();
        else if ("PLAY_PREVIOUS".equals(action))
            playPrevious();
        else
            startForegroundService();

        return START_STICKY;
    }

    public void connectRemote() {
        if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
        } // make sure it does not connect twice, im scared to change this :)

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote sam) {
                spotifyAppRemote = sam; // Say hi to sam :)
                startRemote();
            }

            @Override
            public void onFailure(Throwable error) {
                SpotifyAppRemote.disconnect(spotifyAppRemote);

                ErrorLogActivity.logError("Failed Spotify app remote connection","disconnecting, waiting for spotify to be opened");
            }
        });
    }

    static String currentName = "";
    static String currentArtist ="";
    static String currentImageUrl="";
    static String currentTrackUri="";

    public void startRemote() {
        assert spotifyAppRemote != null;

        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            paused = playerState.isPaused; // Keep track of weather or not playback is paused for the widget

            final Track track = playerState.track;

            // Get information about the current track
            if (track != null) {
                currentName = track.name+"";
                currentArtist = track.artist.name;
                currentTrackUri = track.uri;
                if (track.imageUri.raw != null)
                    currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);

                updateWidget(); // Update the widget whenever this callback is received so it is kept in sync
            } else {
                currentName = "No track is playing";
                currentTrackUri = "";
            }
        });
    }

    public void updateWidget() {
        Context appContext = this.getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int widgetId = R.layout.playback_widget;

        // Update with null configData since this is not updating the widget's customization settings
        PlaybackWidget.updateAppWidget(appContext, appWidgetManager, widgetId, null);
    }

    // playNext(), togglePause(), and playPrevious() are used by the home-screen widget to control playback
    public void playNext() {
        if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
            spotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    public static boolean paused = false;
    public void togglePause() {
        if(spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
            PlayerApi player = spotifyAppRemote.getPlayerApi();

             if (paused)
                 player.resume();
             else
                 player.pause();
        }
    }

    public void playPrevious() {
        if(spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
            spotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }



    // gets next track in queue
    // if the first attempt fails, it will refresh the access token and try again once
    public boolean getNextInQueue() {
        try {
            GetQueue getQueue = new GetQueue();
            boolean getQueueResponse = getQueue.execute().get();
            if (!getQueueResponse) {
                RefreshAccessToken refreshAccessToken = new RefreshAccessToken();
                boolean refreshAccessResponse = refreshAccessToken.execute().get();

                if(refreshAccessResponse) {
                    saveTokens();
                    GetQueue secondGetQueue = new GetQueue();
                    return secondGetQueue.execute().get();
                }
                return false;
            } else
                return true;

        } catch (ExecutionException | InterruptedException e) {
            ErrorLogActivity.logError("Error getting next track in queue","Execution failed epicly >:)");
            return false;
        }
    }

    public void saveTokens() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, "tokens.txt");
            ApiTokens.saveTokens(file);
        }
    }



    // The auto queuing feature of the app works by receiving a broadcast whenever a new track is playing on spotify
    // The app will then look at the current track and the next track in the queue to see if any groups should be activate
    // Example: if the current track is the parent of a "now" group, or the next track is the parent of a "next" group
    // If the proper conditions are met for a group, it's child will be queued
    // Keep in mind that the broadcast is only received once when a new track starts playing, and never again throughout the duration of the track playing
    // The actual check will only be preformed after a delay of 5 seconds from receiving the broadcast

    static String nextTrackUri;
    Timer groupCheckTimer;
    TimerTask checkForGroup;

    private final BroadcastReceiver spotifyReceiver = new BroadcastReceiver() {
        @Override // onReceive is called whenever a new track starts playing
        public void onReceive(Context context, Intent intent) {
            if(spotifyAppRemote == null || !spotifyAppRemote.isConnected())
                connectRemote();

            // Cancel the previous group check if it has not yet executed (i.e. the track was skipped within 5 seconds of starting)
            // This insures that if a track which is part of a group is skipped before playing for 5 seconds, it's child will not be queued
            if(groupCheckTimer != null) {
                groupCheckTimer.cancel();
                groupCheckTimer = null;
            }

            checkForGroup = new TimerTask() {
                @Override
                public void run() {
                    if(getNextInQueue()) {
                        for(int i=0; i<groups.size(); i++) {
                            AutoqueueGroup group = groups.get(i);

                            // 3 conditions to be met if a "now playing" group should be queue its child:
                            // Group condition must be "now"
                            // Parent track must be the same as the current track
                            // Child track must not be the same as the next track,
                            //      there would be no purpose in queuing it since it would already be playing next as it should
                            boolean activateNowPlayingGroup =
                                    Objects.equals(group.getCondition(), "now")
                                            && Objects.equals(group.getParentTrackUri(), currentTrackUri)
                                            && !Objects.equals(group.getChildTrackUri(), nextTrackUri);

                            // 3 conditions to be met if a "next in queue" group should queue its child:
                            // Group condition must be "next"
                            // Parent track must be the same as the next track
                            // Child track must not be the same as the current track,
                            //      there would be no purpose in queuing it since it is playing before its parent as it should
                            boolean activateNextInQueueGroup =
                                    Objects.equals(group.getParentTrackUri(), nextTrackUri)
                                            && Objects.equals(group.getCondition(), "next")
                                            && !Objects.equals(group.getChildTrackUri(), currentTrackUri);

                            // 2 other conditions to be met regardless:
                            // App remote is connected
                            // Group is active
                            boolean remoteIsOk = spotifyAppRemote != null && spotifyAppRemote.isConnected();
                            boolean groupIsActive = group.getActiveState();

                            boolean activateGroup = (activateNowPlayingGroup || activateNextInQueueGroup) && remoteIsOk && groupIsActive;

                            if(activateGroup)
                                spotifyAppRemote.getPlayerApi().queue(group.childTrackUri);

                        }
                    }
                }
            };

            // The checkForGroup task will run after a track has been playing for 5 seconds
            // This allows the user to skip between songs quickly without tracks being queued
            groupCheckTimer = new Timer();
            groupCheckTimer.schedule(checkForGroup, 5000);
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(spotifyReceiver);
        SpotifyAppRemote.disconnect(spotifyAppRemote);
        ErrorLogActivity.logError("SpotifyService destroyed","service for handling background connection has been terminated");
    }
}