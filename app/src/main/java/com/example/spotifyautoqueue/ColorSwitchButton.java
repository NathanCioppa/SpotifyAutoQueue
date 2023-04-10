package com.example.spotifyautoqueue;

public class ColorSwitchButton {
    int buttonId;
    int color;

    public ColorSwitchButton(int buttonId, int color) {
        this.buttonId = buttonId;
        this.color = color;
    }

    public int getId() {return buttonId;}
    public int getColor() {return color;}
}
