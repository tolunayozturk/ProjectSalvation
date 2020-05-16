package com.projectsalvation.pigeotalk.DAO;

public class ContactDAO {

    private String userId;
    private String name;
    private String about;
    private String numberType;
    private String profilePhotoUrl;

    public ContactDAO(String userId,
                      String name,
                      String about,
                      String numberType,
                      String profilePhotoUrl) {
        this.userId = userId;
        this.name = name;
        this.about = about;
        this.numberType = numberType;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
