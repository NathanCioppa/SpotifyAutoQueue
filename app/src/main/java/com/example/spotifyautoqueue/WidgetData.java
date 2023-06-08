package com.example.spotifyautoqueue;

import java.io.Serializable;

public class WidgetData implements Serializable {

    int widgetId;
    int textColor;
    int buttonColor;
    int backgroundColor;
    int backgroundOpacity;
    String layout = "default";

    public WidgetData(int widgetId, int textColor, int buttonColor, int backgroundColor, int backgroundOpacity, String layout) {
        this.widgetId = widgetId;
        this.textColor = textColor;
        this.buttonColor = buttonColor;
        this.backgroundColor = backgroundColor;
        this.backgroundOpacity = backgroundOpacity;
        this.layout = layout;
    }

    public int getWidgetId() { return widgetId; }

    public int getTextColor() { return textColor; }

    public int getButtonColor() { return buttonColor; }

    public int getBackgroundColor() { return backgroundColor; }

    public int getBackgroundOpacity() { return  backgroundOpacity; }

    public String getLayout() { return layout; }

}
