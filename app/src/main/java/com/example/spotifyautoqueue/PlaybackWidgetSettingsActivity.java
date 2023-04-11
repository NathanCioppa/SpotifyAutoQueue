package com.example.spotifyautoqueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class PlaybackWidgetSettingsActivity extends AppCompatActivity {

    int widgetId;
    int[] configData;
    static int textColor = Color.WHITE;
    static int playbackControlColor = Color.WHITE;
    static int backgroundColor = Color.WHITE;
    static int backgroundOpacity = 50;

    ArrayList<ColorSwitchButton> backgroundColors = new ArrayList<>();
    Drawable wallpaperImage;
    WallpaperManager wallpaperManager;

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


        wallpaperManager = WallpaperManager.getInstance(this);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                wallpaperImage = wallpaperManager.getDrawable();
            else
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


        }

        configData = new int[4];

        setupBackgroundColorList();

        setContentView(R.layout.activity_playback_widget_settings);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && wallpaperImage != null)
            findViewById(R.id.widgetConfigActivityBackground).setBackground(wallpaperImage);

        setBackgroundOpacitySliderListener();

        updatePreview(UPDATE_ALL);
        selectCurrentChoices();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            wallpaperImage = wallpaperManager.getDrawable();

            if(wallpaperImage != null)
                findViewById(R.id.widgetConfigActivityBackground).setBackground(wallpaperImage);
        }
    }

    public void setTextBlack(View button) {
        textColor = Color.BLACK;
        updatePreview(TEXT_COLOR_KEY);

        findViewById(R.id.buttonTextWhite).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }

    public void setTextWhite(View button) {
        textColor = Color.WHITE;
        updatePreview(TEXT_COLOR_KEY);

        findViewById(R.id.buttonTextBlack).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }


    public void setPlaybackControlWhite(View button) {
        playbackControlColor = Color.WHITE;
        updatePreview(PLAYBACK_CONTROL_KEY);

        findViewById(R.id.buttonPlaybackBlack).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }

    public void setPlaybackControlBlack(View button) {
        playbackControlColor = Color.BLACK;
        updatePreview(PLAYBACK_CONTROL_KEY);

        findViewById(R.id.buttonPlaybackWhite).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }


    public void setupBackgroundColorList() {
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundBlack, Color.BLACK));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundWhite, Color.WHITE));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundPink, Color.MAGENTA));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundCyan, Color.CYAN));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundGreen, Color.GREEN));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundRed, Color.RED));
    }

    public void setBackgroundColor(View button) {
        for(int i = 0; i < backgroundColors.size(); i++) {
            ColorSwitchButton checkButton = backgroundColors.get(i);
            findViewById(checkButton.getId()).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));

            if(checkButton.getId() == button.getId()) {
                backgroundColor = checkButton.getColor();
                updatePreview(BACKGROUND_KEY);
            }
        }
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }


    SeekBar backgroundOpacitySlider;
    final float FULL_OPACITY = 255;

    public void setBackgroundOpacitySliderListener() {
        backgroundOpacitySlider = findViewById(R.id.backgroundOpacitySlider);

        SeekBar.OnSeekBarChangeListener backgroundOpacitySliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float progress = (FULL_OPACITY/100)*i;

                backgroundOpacity = (int)progress;
                updatePreview(BACKGROUND_KEY);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        backgroundOpacitySlider.setOnSeekBarChangeListener(backgroundOpacitySliderListener);
    }


    public void finishConfig(View button) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        configData[0] = textColor;
        configData[1] = playbackControlColor;
        configData[2] = backgroundColor;
        configData[3] = backgroundOpacity;

        PlaybackWidget.updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId, configData);

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    final int UPDATE_ALL = 0;
    final int TEXT_COLOR_KEY = 1;
    final int PLAYBACK_CONTROL_KEY = 2;
    final int BACKGROUND_KEY = 3;

    public void updatePreview(int key) {
        if(key == TEXT_COLOR_KEY || key == UPDATE_ALL) {
            ((TextView)findViewById(R.id.exampleConfigWidgetName)).setTextColor(textColor);
            ((TextView)findViewById(R.id.exampleConfigWidgetArtist)).setTextColor(textColor);

        }

        if (key == PLAYBACK_CONTROL_KEY || key == UPDATE_ALL) {
            ((ImageButton)findViewById(R.id.exampleConfigWidgetPlaybackNext))
                    .setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));

            ((ImageButton)findViewById(R.id.exampleConfigWidgetPlaybackPause))
                    .setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));

            ((ImageButton)findViewById(R.id.exampleConfigWidgetPlaybackPrevious))
                    .setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));
        }

        if(key == BACKGROUND_KEY || key == UPDATE_ALL) {
            findViewById(R.id.exampleWidgetContainer).getBackground().setAlpha(backgroundOpacity);
            findViewById(R.id.exampleWidgetContainer).getBackground().setTint(backgroundColor);
        }
    }

    public void selectCurrentChoices() {
        if(textColor == Color.WHITE)
            findViewById(R.id.buttonTextWhite).performClick();
        else
            findViewById(R.id.buttonTextBlack).performClick();

        if(playbackControlColor == Color.WHITE)
            findViewById(R.id.buttonPlaybackWhite).performClick();
        else
            findViewById(R.id.buttonPlaybackBlack).performClick();

        for(int i = 0; i < backgroundColors.size(); i++) {
            if (backgroundColors.get(i).color == backgroundColor)
                findViewById(backgroundColors.get(i).getId()).performClick();
        }

        float progressPercent = (100/FULL_OPACITY)*backgroundOpacity;
        backgroundOpacitySlider.setProgress((int)progressPercent);
    }
}