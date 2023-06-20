package com.example.spotifyautoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;

// Class that handles the home-screen playback widget
public class PlaybackWidget extends AppWidgetProvider {

    // Executes whenever a new track plays, callback from app remote is executed, or when the config options are changed
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean isConfigUpdate) {

        try{

            PlaybackWidgetSettingsActivity.getWidgetData(context);

            WidgetData thisWidget = null;
            ArrayList<WidgetData> userWidgetData = PlaybackWidgetSettingsActivity.userWidgetData;

            for(int i=0; i<userWidgetData.size(); i++) {
                if(userWidgetData.get(i).getWidgetId() == appWidgetId) {
                    thisWidget = userWidgetData.get(i);
                    break;
                }
            }

            if(thisWidget == null) {
                ErrorLogActivity.logError("Failed to update a widget","userWidgetData did not contain an id matching the appWidgetId being updated");
                return;
            }

            int layout = Objects.equals(thisWidget.getLayout(), "tall") ? R.layout.playback_widget_tall : R.layout.playback_widget;

            RemoteViews views = new RemoteViews(context.getPackageName(), layout);

            boolean isAdaptiveColor = thisWidget.getBackgroundColor() == BACKGROUND_COLOR_ADAPTIVE;

            String trackImageUrl = SpotifyService.currentImageUrl;
            Uri imageUri = null;
            if(!Objects.equals(trackImageUrl, ""))
                imageUri = Uri.parse(trackImageUrl);

            // Do not update the widget's displayed information if is is just a config update
            if(!isConfigUpdate) {

                views.setTextViewText(R.id.playbackWidgetTrackName, SpotifyService.currentName);
                views.setTextViewText(R.id.playbackWidgetTrackArtist, SpotifyService.currentArtist);

                if(SpotifyService.paused)
                    views.setImageViewResource(R.id.togglePause, thisWidget.getButtonColor() == Color.BLACK
                            ? R.drawable.icons8_play_96___black
                            : R.drawable.icons8_play_96___white
                    );

                if (!SpotifyService.paused)
                    views.setImageViewResource(R.id.togglePause, thisWidget.getButtonColor() == Color.BLACK
                            ? R.drawable.icons8_pause_96___black
                            : R.drawable.icons8_pause_96___white
                    );



                if(imageUri != null) {
                    Glide.with(context).asBitmap().load(imageUri)
                            .into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetId));
                }
            }



            if(isAdaptiveColor && imageUri != null) {
                Glide.with(context).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Palette widgetColorPalette = Palette.from(resource).generate();
                        int color = widgetColorPalette.getVibrantColor(
                                widgetColorPalette.getDarkVibrantColor(
                                        widgetColorPalette.getLightVibrantColor(
                                                widgetColorPalette.getDominantColor(
                                                        Color.BLACK) ) ) );

                        views.setInt(R.id.widgetRoundedBackground, "setColorFilter", color);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
            }

            views.setTextColor(R.id.playbackWidgetTrackName, thisWidget.getTextColor());
            views.setTextColor(R.id.playbackWidgetTrackArtist, thisWidget.getTextColor());

            // Playback buttons can only be toggled between black and white
            // Changes drawables rather than tinting the icon since that does not work on lower device levels
            if(thisWidget.getButtonColor() == Color.WHITE) {
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
            views.setImageViewResource(R.id.widgetRoundedBackground, R.drawable.rounded_corners);
            if(!isAdaptiveColor)
                views.setInt(R.id.widgetRoundedBackground, "setColorFilter", thisWidget.getBackgroundColor());
            views.setInt(R.id.widgetRoundedBackground, "setAlpha", thisWidget.getBackgroundOpacity());



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
            ErrorLogActivity.logError("updateAppWidget","straight up failed execution lol");
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

    // Set the intents for the playback buttons
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

    static final int BACKGROUND_COLOR_ADAPTIVE = 16_000_001;
}