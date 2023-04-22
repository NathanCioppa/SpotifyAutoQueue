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

            getGroups();

            filter = new IntentFilter("com.spotify.music.active");
            registerReceiver(spotifyReceiver, filter); //start to check for spotify to activate again

        } catch (Exception e) {
            ErrorLogActivity.logError("Error starting foreground service", e.toString());
            if(spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                SpotifyAppRemote.disconnect(spotifyAppRemote);
            }
            unregisterReceiver(spotifyReceiver);
        }
    }

    static ArrayList<AutoqueueGroup> groups;

    final String CLIENT_ID = ApiTokens.CLIENT_ID;
    final String REDIRECT_URI = ApiTokens.REDIRECT_URI;
    SpotifyAppRemote spotifyAppRemote;
    Context context = SpotifyService.this;
    IntentFilter filter;


    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

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

    public void getGroups() {
        MainActivity.getGroups(this);
    }

    public void connectRemote() {
        if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
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

    static String currentName = "Name";
    static String currentArtist ="";
    static String currentImageUrl="";

    static String currentTrackUri="";

    public void startRemote() {
        assert spotifyAppRemote != null;

        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            paused = playerState.isPaused;

            final Track track = playerState.track;

            if (track != null) {
                currentName = track.name+"";
                currentArtist = track.artist.name;
                currentTrackUri = track.uri;
                if (track.imageUri.raw != null)
                    currentImageUrl = "https://i.scdn.co/image/"+ track.imageUri.raw.substring(track.imageUri.raw.lastIndexOf(":")+1);

                updateWidget();
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

        PlaybackWidget.updateAppWidget(appContext, appWidgetManager, widgetId, null);
    }

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
            e.printStackTrace();
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

    private final BroadcastReceiver spotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(spotifyAppRemote == null || !spotifyAppRemote.isConnected())
                connectRemote();

            if (getNextInQueue()) {

                if(groupCheckTimer != null) {
                    groupCheckTimer.cancel();
                    groupCheckTimer = null;
                }

                checkForGroup = new TimerTask() {
                    @Override
                    public void run() {
                        for(int i=0; i<groups.size(); i++) {
                            AutoqueueGroup group = groups.get(i);

                            // 3 conditions to be met if a "now playing" group should be queue its child:
                            // group condition must be "now"
                            // parent track must be the same as the current track
                            // child track must not be the same as the next track,
                            //      there would be no purpose in queuing it since it would already be playing next as it should
                            boolean activateNowPlayingGroup =
                                    Objects.equals(group.getCondition(), "now")
                                    && Objects.equals(group.getParentTrackUri(), currentTrackUri)
                                    && !Objects.equals(group.getChildTrackUri(), nextTrackUri);

                            // 3 conditions to be met if a "next in queue" group should queue its child:
                            // group condition must be "next"
                            // parent track must be the same as the next track
                            // child track must not be the same as the current track,
                            //      there would be no purpose in queuing it since it is playing before its parent as it should
                            boolean activateNextInQueueGroup =
                                    Objects.equals(group.getParentTrackUri(), nextTrackUri)
                                    && Objects.equals(group.getCondition(), "next")
                                    && !Objects.equals(group.getChildTrackUri(), currentTrackUri);

                            // 2 other conditions to be met regardless:
                            // app remote is connected
                            // group is active
                            boolean remoteIsOk = spotifyAppRemote != null && spotifyAppRemote.isConnected();
                            boolean groupIsActive = group.getActiveState();

                            boolean activateGroup = (activateNowPlayingGroup || activateNextInQueueGroup) && remoteIsOk && groupIsActive;

                            if(activateGroup)
                                spotifyAppRemote.getPlayerApi().queue(group.childTrackUri);

                        }
                    }
                };

                groupCheckTimer = new Timer();
                groupCheckTimer.schedule(checkForGroup, 5000);
            }
        }
    };


    static String nextTrackUri;
    Timer groupCheckTimer;
    TimerTask checkForGroup;

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
