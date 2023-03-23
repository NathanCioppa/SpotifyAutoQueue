package com.example.spotifyautoqueue;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
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

        //for(int widgetId : appWidgetIds) {
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
        //}



    }

    public void skipToNext(View button) {
        System.out.println("called");
        MainActivity main = new MainActivity();
        //main.skipToNext(button);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("onupdate CALLED");

        for(int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);
            Intent next = new Intent(context, PlaybackWidget.class);
            next.setAction("skipToNext");
        }

    }

    @Override
    public void onEnabled(Context context) {
        System.out.println("onenabled CALLED");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}