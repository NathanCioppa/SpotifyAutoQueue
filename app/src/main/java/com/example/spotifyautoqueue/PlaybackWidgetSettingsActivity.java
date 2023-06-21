package com.example.spotifyautoqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

// Customization settings for the playback widget
public class PlaybackWidgetSettingsActivity extends AppCompatActivity {

    // Initialize to the default config options
    int textColor = Color.WHITE;
    int playbackControlColor = Color.WHITE;
    int backgroundColor = Color.WHITE;
    int backgroundOpacity = 50;
    String layout = "default";

    int widgetId = -1;
    boolean isNewWidget = true;
    int indexOfWidget = -1; // Initialize to -1, it will be changed to its actual id

    WidgetData thisWidget; // WidgetData object to store all information about the widget being configured, be it new or old

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

        // Make sure widgetData is gotten before checking if the widget is new
        getWidgetData(this);
        isNewWidget = checkIfWidgetIsNew();

        // If the widget is not new, thisWidget will be set to its current settings
        if(!isNewWidget) {
            thisWidget = getExistingWidgetData();

            // Sync the widget's settings with the default config options, insures the preview widget looks the same as the widget, not important for a new widget
            textColor = thisWidget.getTextColor();
            playbackControlColor = thisWidget.getButtonColor();
            backgroundColor = thisWidget.getBackgroundColor();
            backgroundOpacity = thisWidget.getBackgroundOpacity();
            layout = thisWidget.getLayout();
        } else
            // thisWidget should be a new WidgetData object with the default settings if the widget is new.
            thisWidget = new WidgetData(widgetId, textColor, playbackControlColor, backgroundColor, backgroundOpacity, layout);

        assert thisWidget != null; // Something has gone very wrong if thisWidget is null at this point

        setContentView(R.layout.activity_playback_widget_settings);

        updatePreview(UPDATE_ALL); // Update all parts of the preview widget to accurately display the settings when the activity is opened
        selectCurrentChoices(); // Select the appropriate buttons for the selected options when the activity is started

        setBackgroundOpacitySliderListener(); // give functionality to the background opacity slider
    }



    // For the color toggle buttons, each button's tag should be parsable to its corresponding color, using Color.parseColor()
    // Docs for parseColor: https://developer.android.com/reference/android/graphics/Color.html#parseColor(java.lang.String)

    public void setTextColor(View selectedButton) {
        int selectedColor = Color.parseColor(selectedButton.getTag().toString());
        textColor = selectedColor;
        updatePreview(TEXT_COLOR_KEY);

        // Only possible colors are black and white, button for non selected color should be grey
        if(selectedColor != Color.BLACK)
            findViewById(R.id.buttonTextBlack).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        else
            findViewById(R.id.buttonTextWhite).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));

        // Make the selected button green
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }

    // Same format as above
    public void setPlaybackControlColor(View selectedButton) {
        int selectedColor = Color.parseColor(selectedButton.getTag().toString());
        playbackControlColor = selectedColor;
        updatePreview(PLAYBACK_CONTROL_KEY);

        if(selectedColor != Color.BLACK)
            findViewById(R.id.buttonPlaybackBlack).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        else
            findViewById(R.id.buttonPlaybackWhite).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));

        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
    }

    public void setBackgroundColor(View selectedButton) {
        String tag = selectedButton.getTag().toString();
        if(Objects.equals(tag, "adaptive"))
            backgroundColor = PlaybackWidget.BACKGROUND_COLOR_ADAPTIVE;
        else
            backgroundColor = Color.parseColor(tag);

        ViewGroup buttons = findViewById(R.id.backgrounColorButtonsContainer);
        updatePreview(BACKGROUND_KEY);

        for(int i=0; i<buttons.getChildCount(); i++) {
            View thisButton = buttons.getChildAt(i);

            thisButton.setBackgroundTintList(ContextCompat.getColorStateList(this,
                    thisButton.getId() == selectedButton.getId()
                    ? R.color.spotify_logo_green
                    : R.color.middle_grey
            ));
        }
    }



    SeekBar backgroundOpacitySlider;
    final float FULL_OPACITY = 255; // Opacity is an alpha value ranging from 0 to 255

    // Sets up the listener for the functionality of the background opacity slider
    public void setBackgroundOpacitySliderListener() {
        backgroundOpacitySlider = findViewById(R.id.backgroundOpacitySlider);

        SeekBar.OnSeekBarChangeListener backgroundOpacitySliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int seekBarProgress, boolean b) {
                // Convert the seekBar's progress (0 to 100) into an alpha value (0 to 255)
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



    // Tag of the selected button should only be "default" or "tall"
    public void selectLayout(View selectedButton) {
        layout = selectedButton.getTag().toString();

        if(!Objects.equals(layout,"default"))
            findViewById(R.id.buttonLayoutDefault).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));
        else
            findViewById(R.id.buttonLayoutTall).setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.middle_grey));

        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.spotify_logo_green));
        updatePreview(LAYOUT_KEY);
    }



    public void finishConfig(View button) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        thisWidget.textColor = textColor;
        thisWidget.buttonColor = playbackControlColor;
        thisWidget.backgroundColor = backgroundColor;
        thisWidget.backgroundOpacity = backgroundOpacity;
        thisWidget.layout = layout;

        if(isNewWidget)
            userWidgetData.add(thisWidget);
        else
            userWidgetData.set(indexOfWidget, thisWidget);

        saveWidgetData(this);

        PlaybackWidget.updateAppWidget(this.getApplicationContext(), appWidgetManager, widgetId, true);

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }


    View exampleWidgetContainer;
    TextView exampleWidgetName;
    TextView exampleWidgetArtist;
    ImageButton exampleWidgetPlaybackNext;
    ImageButton exampleWidgetPlaybackPause;
    ImageButton exampleWidgetPlaybackPrevious;

    final int UPDATE_ALL = 0;
    final int TEXT_COLOR_KEY = 1;
    final int PLAYBACK_CONTROL_KEY = 2;
    final int BACKGROUND_KEY = 3;
    final int LAYOUT_KEY = 4;

    // Updates the preview of the widget displayed in the activity
    // Should always be called when a change is made to the config settings, with the appropriate key passed as an argument (keys are above)
    public void updatePreview(int key) {
        if(key == LAYOUT_KEY)
            key = UPDATE_ALL; // All need to be updated if updating the layout

        if(key == UPDATE_ALL) {
            if(Objects.equals(layout, "default")) {
                exampleWidgetContainer = findViewById(R.id.exampleWidgetContainer);
                exampleWidgetName = findViewById(R.id.exampleWidgetName);
                exampleWidgetArtist = findViewById(R.id.exampleWidgetArtist);
                exampleWidgetPlaybackNext = findViewById(R.id.exampleWidgetPlaybackNext);
                exampleWidgetPlaybackPause = findViewById(R.id.exampleWidgetPlaybackPause);
                exampleWidgetPlaybackPrevious = findViewById(R.id.exampleWidgetPlaybackPrevious);

                findViewById(R.id.exampleWidgetContainerTall).setVisibility(View.GONE);
                exampleWidgetContainer.setVisibility(View.VISIBLE);
            }
            if(Objects.equals(layout, "tall")) {
                exampleWidgetContainer = findViewById(R.id.exampleWidgetContainerTall);
                exampleWidgetName = findViewById(R.id.exampleWidgetNameTall);
                exampleWidgetArtist = findViewById(R.id.exampleWidgetArtistTall);
                exampleWidgetPlaybackNext = findViewById(R.id.exampleWidgetPlaybackNextTall);
                exampleWidgetPlaybackPause = findViewById(R.id.exampleWidgetPlaybackPauseTall);
                exampleWidgetPlaybackPrevious = findViewById(R.id.exampleWidgetPlaybackPreviousTall);

                findViewById(R.id.exampleWidgetContainer).setVisibility(View.GONE);
                exampleWidgetContainer.setVisibility(View.VISIBLE);
            }
        }

        if(key == TEXT_COLOR_KEY || key == UPDATE_ALL) {
            exampleWidgetName.setTextColor(textColor);
            exampleWidgetArtist.setTextColor(textColor);

        }

        if (key == PLAYBACK_CONTROL_KEY || key == UPDATE_ALL) {
            exampleWidgetPlaybackNext.setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));

            exampleWidgetPlaybackPause.setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));

            exampleWidgetPlaybackPrevious.setImageTintList(ContextCompat.getColorStateList(this,
                            playbackControlColor == Color.WHITE
                                    ? R.color.white
                                    : R.color.black
                    ));
        }

        if(key == BACKGROUND_KEY || key == UPDATE_ALL) {
            exampleWidgetContainer.getBackground().setAlpha(backgroundOpacity);
            exampleWidgetContainer.getBackground()
                    .setTint(backgroundColor == PlaybackWidget.BACKGROUND_COLOR_ADAPTIVE
                            ? Color.BLACK
                            : backgroundColor);
        }
    }

    public void selectCurrentChoices() {
        // Perform a click on the button corresponding the whatever is set when the activity is started
        // Insures that the button for the current choice is the active button

        // Text color and playback control color only have 2 options
        if(textColor == Color.WHITE)
            findViewById(R.id.buttonTextWhite).performClick();
        else
            findViewById(R.id.buttonTextBlack).performClick();

        if(playbackControlColor == Color.WHITE)
            findViewById(R.id.buttonPlaybackWhite).performClick();
        else
            findViewById(R.id.buttonPlaybackBlack).performClick();

        // Background color buttons are all contained in the same constraint layout
        ViewGroup backgroundButtons = findViewById(R.id.backgrounColorButtonsContainer);
        for (int i=0; i<backgroundButtons.getChildCount(); i++) {
            View thisButton = backgroundButtons.getChildAt(i);
            if(Objects.equals(thisButton.getTag().toString(),"adaptive")) {
                thisButton.performClick();
                break;
            }else if(Color.parseColor(thisButton.getTag().toString()) == backgroundColor) {
                thisButton.performClick();
                break;
            }
        }

        // Set the progress of the opacity slider seekBar
        float progressPercent = (100/FULL_OPACITY)*backgroundOpacity;
        ((SeekBar)findViewById(R.id.backgroundOpacitySlider)).setProgress((int)progressPercent);

        if(Objects.equals(layout, "default"))
            findViewById(R.id.buttonLayoutDefault).performClick();
        else
            findViewById(R.id.buttonLayoutTall).performClick();
    }



    static ArrayList<WidgetData> userWidgetData = new ArrayList<>(); // ArrayList containing information about all the user's widgets

    // Get the current settings of a widget if it is not just being created
    public boolean checkIfWidgetIsNew() {
        assert widgetId != -1;

        for(int i=0; i<userWidgetData.size(); i++) {
            if(userWidgetData.get(i).getWidgetId() == widgetId)
                return false;
        }
        return true;
    }

    public WidgetData getExistingWidgetData() {
        for(int i=0; i<userWidgetData.size(); i++) {
            if(userWidgetData.get(i).getWidgetId() == widgetId) {
                indexOfWidget = i;
                return userWidgetData.get(indexOfWidget);
            }
        }
        return null;
    }

    public static void saveWidgetData(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("widgets.txt", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(userWidgetData);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getWidgetData(Context context) {
        try {
            FileInputStream fis = context.openFileInput("widgets.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            userWidgetData = (ArrayList<WidgetData>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Removes leftover WidgetData from userWidgetData, so that data from widgets which have been deleted is not stored
    public static void removeExtraUserWidgetInfo(Context context) {
        getWidgetData(context);

        ComponentName componentName = new ComponentName(context.getApplicationContext(), PlaybackWidget.class);
        int[] activeAppWidgetIds = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(componentName);
        Arrays.sort(activeAppWidgetIds);

        ArrayList<WidgetData> filteredStoredData = new ArrayList<>();

        for (int i=0; i<userWidgetData.size(); i++) {
            if(Arrays.binarySearch(activeAppWidgetIds, userWidgetData.get(i).getWidgetId()) >= 0){
                filteredStoredData.add(userWidgetData.get(i));
            }
        }

        userWidgetData = filteredStoredData;
        saveWidgetData(context);
    }
}