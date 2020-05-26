package com.projectsalvation.pigeotalk.DAO;

public class UserDAO {

    private String userID;
    private String displayName;
    private String about;
    private String photoUrl;

    public UserDAO(String userID, String displayName, String about, String photoUrl) {
        this.userID = userID;
        this.displayName = displayName;
        this.about = about;
        this.photoUrl = photoUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
