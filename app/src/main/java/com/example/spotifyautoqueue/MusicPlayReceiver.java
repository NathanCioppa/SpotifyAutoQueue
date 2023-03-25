package com.example.spotifyautoqueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicPlayReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RECIEVER","called");
        if (intent.getAction().equals("com.spotify.music.playbackstatechanged")) {
            Log.d("RECIEVER","playback state changed");
            Log.d("EXTRAS",intent.getExtras().toString());
        }
    }
}
