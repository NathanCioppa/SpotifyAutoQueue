package com.example.spotifyautoqueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class PlaybackWidgetReceiver extends BroadcastReceiver {
    private SpotifyAppRemote spotifyAppRemote;

    public PlaybackWidgetReceiver(SpotifyAppRemote spotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote;
    }

    public void PlaybackWidgetBroadcastReceiver(SpotifyAppRemote spotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("received");

        String action = intent.getAction();
        if(action.equals("SKIP_TO_NEXT")) {
            //spotifyAppRemote.getPlayerApi().skipNext();
            System.out.println("next");
        }
        if(action.equals("TOGGLE_PAUSE")) {
            System.out.println("pause");
        }
        if(action.equals("BACK_TO_PREVIOUS")) {
            System.out.println("prev");
        }
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("SKIP_TO_NEXT");
        filter.addAction("TOGGLE_PAUSE");
        filter.addAction("BACK_TO_PREVIOUS");

        return filter;
    }
}
