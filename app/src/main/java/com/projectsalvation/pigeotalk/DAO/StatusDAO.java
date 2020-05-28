package com.projectsalvation.pigeotalk.DAO;

public class StatusDAO {

    private String photoUrl;
    private String timestamp;
    private String displayName;

    public StatusDAO(String photoUrl, String timestamp, String displayName) {
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
