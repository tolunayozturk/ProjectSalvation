package com.projectsalvation.pigeotalk.Database;

public class User {
    private String userId;
    private String userPhoneNumber;
    private String userName;
    private String userAbout;
    private String userProfilePhotoUrl;

    public User() {
    }

    public User(String userId, String phoneNumber, String name, String about, String profilePhotoUrl) {
        this.userId = userId;
        this.userPhoneNumber = phoneNumber;
        this.userName = name;
        this.userAbout = about;
        this.userProfilePhotoUrl = profilePhotoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAbout() {
        return userAbout;
    }

    public void setUserAbout(String userAbout) {
        this.userAbout = userAbout;
    }

    public String getUserProfilePhotoUrl() {
        return userProfilePhotoUrl;
    }

    public void setUserProfilePhotoUrl(String userProfilePhotoUrl) {
        this.userProfilePhotoUrl = userProfilePhotoUrl;
    }
}
