package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


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
                Log.e("MainActivity", error.getMessage(), error );
            }
        });
    }

    //ok so apparently this function is already called in the background basically whenever a new song plays anyway
    //which is fucking amazing for me.
    private void connected() {
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            final Track track = playerState.track;
            if (track != null) {
                Log.d("MainActivity", track.name + " by " + track.artist.name);
            } else {
                Log.d("MainActivity", "No track is playing");
            }

            GetQueue getQueue = new GetQueue();
            getQueue.execute();

        });
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