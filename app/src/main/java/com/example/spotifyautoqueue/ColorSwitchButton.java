package com.example.spotifyautoqueue;

// Used in the widget config activity when selecting colors
// The ID of each button should be given a corresponding int to represent a color
public class ColorSwitchButton {

    int buttonId;
    int color;

    public ColorSwitchButton(int buttonId, int color) {
        this.buttonId = buttonId;
        this.color = color;
    }

    public int getId() { return buttonId; }
    public int getColor() { return color; }

}
