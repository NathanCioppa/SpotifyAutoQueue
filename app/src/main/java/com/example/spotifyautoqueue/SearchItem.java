package com.example.spotifyautoqueue;

public class SearchItem {
    String name;
    String artist;
    String imageUri;
    String uri;

    public SearchItem(String name, String artist, String imageUri, String uri){
        this.name = name;
        this.artist = artist;
        this.imageUri = imageUri;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getUri() {
        return uri;
    }
}
