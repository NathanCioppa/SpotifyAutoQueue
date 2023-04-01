package com.example.spotifyautoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class PlaybackWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        try{
            ComponentName componentName = new ComponentName(context, PlaybackWidget.class);
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(componentName);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);

            String trackImageUrl = SpotifyService.currentImageUrl;
            Uri imageUri = Uri.parse(trackImageUrl);

            views.setTextViewText(R.id.playbackWidgetTrackName, SpotifyService.currentName);
            views.setTextViewText(R.id.playbackWidgetTrackArtist, SpotifyService.currentArtist);

            try {
                Glide.with(context)
                        .asBitmap().
                        load(imageUri).
                        into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
            } catch (Exception e) {
                ErrorLogActivity.logError("update widget image", "failed to load image to widget");
            }

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

    public void setButtonAction(Context context, RemoteViews views, int buttonId, String action) {
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