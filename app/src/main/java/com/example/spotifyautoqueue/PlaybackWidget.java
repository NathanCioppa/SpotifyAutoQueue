package com.example.spotifyautoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

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

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}