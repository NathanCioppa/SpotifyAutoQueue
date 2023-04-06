package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.WindowCompat;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.TextView;

public class PlaybackWidgetSettingsActivity extends AppCompatActivity {

    int widgetId;
    int[] configData;
    static int textColor = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        updatePreview(UPDATE_ALL);
        selectCurrentChoices();
    }

    public void setTextBlack(View button) {
        textColor = Color.BLACK;
        updatePreview(TEXT_COLOR_KEY);
    }

    public void setTextWhite(View button) {
        textColor = Color.WHITE;
        updatePreview(TEXT_COLOR_KEY);
    }

    public void finishConfig(View button) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        configData[0] = textColor;

        PlaybackWidget.updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId, configData);

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    final int UPDATE_ALL = 0;
    final int TEXT_COLOR_KEY = 1;
    final int BUTTON_COLOR_KEY = 2;
    final int BACKGROUND_COLOR_KEY = 3;
    public void updatePreview(int key) {
        if(key == TEXT_COLOR_KEY || key == UPDATE_ALL) {
            ((TextView)findViewById(R.id.exampleConfigWidgetName)).setTextColor(textColor);
            ((TextView)findViewById(R.id.exampleConfigWidgetArtist)).setTextColor(textColor);

        }
        //if (key == BUTTON_COLOR_KEY || key == UPDATE_ALL) {

        //}
    }

    public void selectCurrentChoices() {
        if(textColor == Color.WHITE)
            ((RadioButton)findViewById(R.id.buttonTextWhite)).performClick();
        else
            ((RadioButton)findViewById(R.id.buttonTextBlack)).performClick();
    }
}