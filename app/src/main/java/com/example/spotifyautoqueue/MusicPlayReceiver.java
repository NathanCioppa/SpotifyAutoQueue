package com.example.spotifyautoqueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicPlayReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RECEIVER","called");
        if (intent.getAction().equals("com.spotify.music.playbackstatechanged")) {
            Log.d("RECEIVER","playback state changed");
        }
    }
}
