package com.example.spotifyautoqueue;

public class AutoqueueGroup {
    String parentTitle;
    String parentTrackUri;
    String parentImageUrl;

    String childTitle;
    String childTrackUri;
    String childImageUrl;

    String condition;

    public AutoqueueGroup(String parentTitle, String parentTrackUri, String parentImageUri,
                          String childTitle, String childTitleUri, String childImageUri,
                          String condition
    ){
        this.parentTitle = parentTitle;
        this.parentTrackUri = parentTrackUri;
        this.parentImageUrl = parentImageUri;

        this.childTitle = childTitle;
        this.childTrackUri = childTitleUri;
        this.childImageUrl = childImageUri;

        this.condition = condition;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public String getParentTrackUri() {
        return parentTrackUri;
    }

    public String getParentImageUrl() {
        return parentImageUrl;
    }

    public String getChildTitle() {
        return childTitle;
    }

    public String getChildTrackUri() {
        return childTrackUri;
    }

    public String getChildImageUrl() {
        return childImageUrl;
    }

    public String getCondition() {
        return condition;
    }
}
