package com.example.spotifyautoqueue;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.Objects;

public class PlaybackWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] configData) {

        try{
            ComponentName componentName = new ComponentName(context, PlaybackWidget.class);
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(componentName);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);

            if(configData == null) {

                views.setTextViewText(R.id.playbackWidgetTrackName, SpotifyService.currentName);
                views.setTextViewText(R.id.playbackWidgetTrackArtist, SpotifyService.currentArtist);

                if(SpotifyService.paused)
                    views.setImageViewResource(R.id.togglePause, R.drawable.icons8_play_96___);
                else
                    views.setImageViewResource(R.id.togglePause, R.drawable.icons8_pause_96___);

                String trackImageUrl = SpotifyService.currentImageUrl;
                if(!Objects.equals(trackImageUrl, "")) {
                    Uri imageUri = Uri.parse(trackImageUrl);
                        Glide.with(context).asBitmap().load(imageUri)
                            .into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
                }

            } else {

                final int TEXT_COLOR = configData[0];
                final int PLAYBACK_CONTROL_COLOR = configData[1];
                final int BACKGROUND_COLOR = configData[2];
                final int BACKGROUND_OPACITY = configData[3];

                views.setTextColor(R.id.playbackWidgetTrackName, TEXT_COLOR);
                views.setTextColor(R.id.playbackWidgetTrackArtist, TEXT_COLOR);


                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    views.setColorStateList(R.id.togglePause, "setImageTintList", ContextCompat.getColorStateList(context,
                            PLAYBACK_CONTROL_COLOR == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));
                    views.setColorStateList(R.id.playPrevious, "setImageTintList", ContextCompat.getColorStateList(context,
                            PLAYBACK_CONTROL_COLOR == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));
                    views.setColorStateList(R.id.playNext, "setImageTintList", ContextCompat.getColorStateList(context,
                            PLAYBACK_CONTROL_COLOR == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));



                } else
                    ErrorLogActivity.logError("Error setting widget config","Can not set playback control button colors, requires Android 12 (API level 31) or higher");

                views.setImageViewResource(R.id.imageView333, R.drawable.rounded_corners);
                views.setInt(R.id.imageView333, "setColorFilter", BACKGROUND_COLOR);
                views.setInt(R.id.imageView333, "setAlpha", BACKGROUND_OPACITY);
            }

            Intent openApp = new Intent(context, MainActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingOpen);

            setButtonAction(context, views, R.id.playNext, PLAY_NEXT_WIDGET);
            setButtonAction(context, views, R.id.togglePause, TOGGLE_PAUSE_WIDGET);
            setButtonAction(context, views, R.id.playPrevious, PLAY_PREVIOUS_WIDGET);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            ErrorLogActivity.logError("updateAppWidget","straight up failed execution");
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int appWidgetId : appWidgetIds) {

            Intent openApp = new Intent(context, MainActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingOpen);

            setButtonAction(context, views, R.id.playNext, PLAY_NEXT_WIDGET);
            setButtonAction(context, views, R.id.togglePause, TOGGLE_PAUSE_WIDGET);
            setButtonAction(context, views, R.id.playPrevious, PLAY_PREVIOUS_WIDGET);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public static void setButtonAction(Context context, RemoteViews views, int buttonId, String action) {
        Intent intent = new Intent(context, PlaybackWidget.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(buttonId, pendingIntent);
    }

    public static final String PLAY_NEXT_WIDGET = "com.example.spotifyautoqueue.PLAY_NEXT_WIDGET";
    public static final String TOGGLE_PAUSE_WIDGET = "com.example.spotifyautoqueue.TOGGLE_PAUSE_WIDGET";
    public static final String PLAY_PREVIOUS_WIDGET = "com.example.spotifyautoqueue.PLAY_PREVIOUS_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if(action.equals(PLAY_NEXT_WIDGET))
            spotifyServiceAction(context, "PLAY_NEXT");

        if(action.equals(TOGGLE_PAUSE_WIDGET))
            spotifyServiceAction(context, "TOGGLE_PAUSE");

        if(action.equals(PLAY_PREVIOUS_WIDGET))
            spotifyServiceAction(context, "PLAY_PREVIOUS");
    }

    public void spotifyServiceAction(Context context, String action) {
        Intent intent = new Intent(context, SpotifyService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}