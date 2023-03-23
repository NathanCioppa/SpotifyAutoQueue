package com.example.spotifyautoqueue;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

import org.w3c.dom.Text;

/**
 * Implementation of App Widget functionality.
 */
public class PlaybackWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Context appContext = context.getApplicationContext();

        ComponentName componentName = new ComponentName(context, PlaybackWidget.class);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(componentName);

        String trackImageUrl = MainActivity.currentImageUrl;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);

        Uri imageUri = Uri.parse(trackImageUrl);
        try{
            Glide.with(context)
                    .asBitmap().
                    load(imageUri).
                    into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
        }catch (Exception e){
            e.printStackTrace();
        };

        views.setTextViewText(R.id.playbackWidgetTrackName, MainActivity.currentName);
        views.setTextViewText(R.id.playbackWidgetTrackArtist, MainActivity.currentArtist);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        /*for (int appWidgetId : appWidgetIds) {

            String trackImageUrl = MainActivity.currentImageUrl;
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);

            Uri imageUri = Uri.parse(trackImageUrl);
            try{
                Glide.with(context)
                        .asBitmap().
                        load(imageUri).
                        into(new AppWidgetTarget(context, R.id.widgetImage, views, appWidgetIds));
            }catch (Exception e){
                e.printStackTrace();
            };


            updateAppWidget(context, appWidgetManager, appWidgetId);
        }*/
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}