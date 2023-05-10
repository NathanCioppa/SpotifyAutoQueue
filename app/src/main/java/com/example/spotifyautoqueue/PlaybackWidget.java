package com.example.spotifyautoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

import java.util.Arrays;
import java.util.Objects;

// Class that handles the home-screen playback widget
public class PlaybackWidget extends AppWidgetProvider {

    // Executes whenever a new track plays, or when the config options are changed
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] configData) {

        try{
            System.out.println("f " +appWidgetId);
            ComponentName componentName = new ComponentName(context, PlaybackWidget.class);
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(componentName);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);
            System.out.println("s "+ Arrays.toString(appWidgetIds));

            // only update playback information, since config data is not being changed
            if(configData == null) {

                views.setTextViewText(R.id.playbackWidgetTrackName, SpotifyService.currentName);
                views.setTextViewText(R.id.playbackWidgetTrackArtist, SpotifyService.currentArtist);

                if(SpotifyService.paused)

                    views.setImageViewResource(R.id.togglePause, R.drawable.icons8_play_96___black);
                else
                    views.setImageViewResource(R.id.togglePause, R.drawable.icons8_pause_96___black);

                String trackImageUrl = SpotifyService.currentImageUrl;
                if(!Objects.equals(trackImageUrl, "")) {
                    Uri imageUri = Uri.parse(trackImageUrl);
                        Glide.with(context).asBitmap().load(imageUri)
                            .into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
                }

            // configData was changed, update the widget accordingly without changing playback data
            } else {

                final int TEXT_COLOR = configData[0];
                final int PLAYBACK_CONTROL_COLOR = configData[1];
                final int BACKGROUND_COLOR = configData[2];
                final int BACKGROUND_OPACITY = configData[3];

                views.setTextColor(R.id.playbackWidgetTrackName, TEXT_COLOR);
                views.setTextColor(R.id.playbackWidgetTrackArtist, TEXT_COLOR);

                // Playback buttons can only be toggled between black and white
                if(PLAYBACK_CONTROL_COLOR == Color.WHITE) {
                    views.setImageViewResource(R.id.togglePause, SpotifyService.paused
                            ? R.drawable.icons8_play_96___white
                            : R.drawable.icons8_pause_96___white
                    );
                    views.setImageViewResource(R.id.playNext, R.drawable.icons8_end_96___white);
                    views.setImageViewResource(R.id.playPrevious, R.drawable.icons8_skip_to_start_96___white);
                } else {
                    views.setImageViewResource(R.id.togglePause, SpotifyService.paused
                            ? R.drawable.icons8_play_96___black
                            : R.drawable.icons8_pause_96___black
                    );
                    views.setImageViewResource(R.id.playNext, R.drawable.icons8_end_96___black);
                    views.setImageViewResource(R.id.playPrevious, R.drawable.icons8_skip_to_start_96___black);
                }

                // Set widget background drawable, color, and opacity
                views.setImageViewResource(R.id.imageView333, R.drawable.rounded_corners);
                views.setInt(R.id.imageView333, "setColorFilter", BACKGROUND_COLOR);
                views.setInt(R.id.imageView333, "setAlpha", BACKGROUND_OPACITY);
            }

            // Set the intent to open the MainActivity when the widget it pressed
            Intent openApp = new Intent(context, MainActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingOpen);

            // Set onclick functions for the playback buttons
            setButtonAction(context, views, R.id.playNext, PLAY_NEXT_WIDGET);
            setButtonAction(context, views, R.id.togglePause, TOGGLE_PAUSE_WIDGET);
            setButtonAction(context, views, R.id.playPrevious, PLAY_PREVIOUS_WIDGET);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            ErrorLogActivity.logError("updateAppWidget","straight up failed execution");
        }
    }


    // I don't think this does what it's supposed to but it's here anyway :)
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

    // Set the intents for the playback buttons, which is annoyingly difficult to do
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
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}