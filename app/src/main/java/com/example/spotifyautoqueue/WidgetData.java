package com.example.spotifyautoqueue;

import java.io.Serializable;

public class WidgetData implements Serializable {

    int widgetId;
    int textColor;
    int buttonColor;
    int backgroundColor;

    public WidgetData(int widgetId, int textColor, int buttonColor, int backgroundColor) {
        this.widgetId = widgetId;
        this.textColor = textColor;
        this.buttonColor = buttonColor;
        this.backgroundColor = backgroundColor;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

}
