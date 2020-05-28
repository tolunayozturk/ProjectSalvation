package com.projectsalvation.pigeotalk.DAO;

public class StatusDAO {

    private String photoUrl;
    private String timestamp;

    public StatusDAO(String photoUrl, String timestamp) {
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
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
}
