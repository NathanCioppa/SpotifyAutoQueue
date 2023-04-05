package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

public class PlaybackWidgetSettingsActivity extends AppCompatActivity {

    int widgetId;
    TextView exampleName;
    TextView exampleArtist;

    int[] configData;
    static int textColor = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exampleName = findViewById(R.id.exampleName);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        configData = new int[3];
        setContentView(R.layout.activity_playback_widget_settings);
    }

    public void setTextBlack(View button) {
        textColor = Color.BLACK;
        try{
            exampleName.setTextColor(Color.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTextWhite(View button) {
        textColor = Color.WHITE;
    }

    public void finishConfig(View button) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        configData[0] = textColor;

        PlaybackWidget.updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId, configData);

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}