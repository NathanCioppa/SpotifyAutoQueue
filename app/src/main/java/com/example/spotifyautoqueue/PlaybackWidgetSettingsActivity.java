package com.example.spotifyautoqueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.ArrayList;

// Customization settings for the playback widget
public class PlaybackWidgetSettingsActivity extends AppCompatActivity {

    int widgetId;
    int[] configData;

    // Initialize to the default config options
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

        // Get the wallpaper to be used as the background of the config activity, so the user can see a more accurate preview of the widget
        // Make sure the READ_EXTERNAL_STORAGE permission is granted, request the permission if not
        // Android API level 33 makes it stupidly difficult to get the wallpaper, so it just wont do that :)
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

        updatePreview(UPDATE_ALL); // Update all parts of the preview widget to accurately reflect the settings when the activity is opened
        selectCurrentChoices(); // Select the appropriate buttons for the selected options when the activity is started
    }

    // Requests the READ_EXTERNAL_STORAGE permission to access the wallpaper, sets the background image if permission is granted
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

    // Sets up the ArrayList of available background colors, I need to make this better
    public void setupBackgroundColorList() {
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundBlack, Color.BLACK));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundWhite, Color.WHITE));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundPink, Color.MAGENTA));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundCyan, Color.CYAN));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundGreen, Color.GREEN));
        backgroundColors.add(new ColorSwitchButton(R.id.buttonBackgroundRed, Color.RED));
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
    final float FULL_OPACITY = 255; // Opacity is an alpha value ranging from 0 to 255

    // Sets up the listener for the functionality of the background opacity slider
    public void setBackgroundOpacitySliderListener() {
        backgroundOpacitySlider = findViewById(R.id.backgroundOpacitySlider);

        SeekBar.OnSeekBarChangeListener backgroundOpacitySliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int seekBarProgress, boolean b) {
                // Convert the seekBar's progress (0 to 100) into an alpha value (0 t0 255)
                float alpha = (FULL_OPACITY/100)*seekBarProgress;

                backgroundOpacity = (int)alpha;
                updatePreview(BACKGROUND_KEY);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        backgroundOpacitySlider.setOnSeekBarChangeListener(backgroundOpacitySliderListener);
    }

    final int TEXT_COLOR = 0;
    final int PLAYBACK_CONTROL_COLOR = 1;
    final int BACKGROUND_COLOR = 2;
    final int BACKGROUND_OPACITY = 3;

    public void finishConfig(View button) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // Add the user's changes into the configData array, update the widget with the configData
        configData[TEXT_COLOR] = textColor;
        configData[PLAYBACK_CONTROL_COLOR] = playbackControlColor;
        configData[BACKGROUND_COLOR] = backgroundColor;
        configData[BACKGROUND_OPACITY] = backgroundOpacity;

        PlaybackWidget.updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId, configData);

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    final int UPDATE_ALL = 0;
    final int TEXT_COLOR_KEY = 1;
    final int PLAYBACK_CONTROL_KEY = 2;
    final int BACKGROUND_KEY = 3;

    // Updates the preview of the widget displayed in the activity
    // Should always be called when a change is made to the config settings, with the appropriate key passed as an argument (keys are above)
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
        // Perform a click on the button corresponding the whatever is set when the activity is started
        // Insures that the button for the current choice is the active button
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

        // Set the progress of the opacity slider seekBar
        float progressPercent = (100/FULL_OPACITY)*backgroundOpacity;
        backgroundOpacitySlider.setProgress((int)progressPercent);
    }
}