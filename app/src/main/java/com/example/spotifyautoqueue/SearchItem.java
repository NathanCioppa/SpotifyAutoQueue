package com.example.spotifyautoqueue;

// Object which displays an item from a spotify search when creating a group
public class SearchItem {

    String name;
    String artist;
    String imageUrl;
    String uri;

    public SearchItem(String name, String artist, String imageUrl, String uri){
        this.name = name;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.uri = uri;
    }

    public String getName() { return name; }

    public String getArtist() { return artist; }

    public String getImageUrl() { return imageUrl; }
}
