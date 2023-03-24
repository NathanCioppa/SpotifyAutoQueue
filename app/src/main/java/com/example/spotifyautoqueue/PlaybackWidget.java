package com.example.spotifyautoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

/**
 * Implementation of App Widget functionality.
 */
public class PlaybackWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        ComponentName componentName = new ComponentName(context, PlaybackWidget.class);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(componentName);

        String trackImageUrl = SpotifyService.currentImageUrl;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);

        Uri imageUri = Uri.parse(trackImageUrl);

        views.setTextViewText(R.id.playbackWidgetTrackName, SpotifyService.currentName);
        views.setTextViewText(R.id.playbackWidgetTrackArtist, SpotifyService.currentArtist);

            try{
                Glide.with(context)
                        .asBitmap().
                        load(imageUri).
                        into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
            }catch (Exception e){
                e.printStackTrace();
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
            ErrorLogActivity.logError("Widget updateAppWidget","finished execution of updateAppWidget in PlaybackWidget");

    }

    public void skipToNext(View button) {
        System.out.println("called");
        MainActivity main = new MainActivity();
        //main.skipToNext(button);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("onUpdate CALLED");

        for(int appWidgetId : appWidgetIds) {
            Intent openApp = new Intent(context, MainActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingOpen);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        ErrorLogActivity.logError("Widget onUpdate","finished execution of onUpdate in PlaybackWidget");
    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("onEnabled CALLED");
        ErrorLogActivity.logError("Widget onEnable","finished execution on onEnabled in playbackWidget");
    }

    @Override
    public void onDisabled(Context context) {
        ErrorLogActivity.logError("Widget onDisabled","finished execution on onDisabled in playbackWidget");
    }
}