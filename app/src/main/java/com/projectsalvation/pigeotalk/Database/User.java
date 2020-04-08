package com.projectsalvation.pigeotalk.Database;

public class User {
    private String userPhone;
    private String userFullName;
    private String userAbout;
    private String userProfilePicture;

    public User() {
    }

    public User(String userPhone, String userFullName, String userAbout, String userProfilePicture) {
        this.userPhone = userPhone;
        this.userFullName = userFullName;
        this.userAbout = userAbout;
        this.userProfilePicture = userProfilePicture;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserAbout() {
        return userAbout;
    }

    public void setUserAbout(String userAbout) {
        this.userAbout = userAbout;
    }

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }
}
