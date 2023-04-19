package com.example.spotifyautoqueue;

import java.io.Serializable;

public class AutoqueueGroup implements Serializable {
    String parentTitle;
    String parentTrackUri;
    String parentImageUrl;

    String childTitle;
    String childTrackUri;
    String childImageUrl;

    String condition;
    boolean activeState;
    long id;

    public AutoqueueGroup(String parentTitle, String parentTrackUri, String parentImageUri,
                          String childTitle, String childTitleUri, String childImageUri,
                          String condition, boolean activeState, long id
    ){
        this.parentTitle = parentTitle;
        this.parentTrackUri = parentTrackUri;
        this.parentImageUrl = parentImageUri;

        this.childTitle = childTitle;
        this.childTrackUri = childTitleUri;
        this.childImageUrl = childImageUri;

        this.condition = condition;
        this.activeState = activeState;
        this.id = id;
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
    public boolean getActiveState() {
        return activeState;
    }
    public long getId() {
        return id;
    }
}
