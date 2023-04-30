package com.example.spotifyautoqueue;

// Error message object
public class ErrorMessage {
    String time;
    String tag;
    String message;

    public ErrorMessage(String time, String tag, String message) {
        this.time = time;
        this.tag = tag;
        this.message = message;
    }

    public String getTime() {return time;}
    public String getTag() {return tag;}
    public String getMessage() {return  message;}
}
