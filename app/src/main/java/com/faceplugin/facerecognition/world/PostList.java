package com.faceplugin.facerecognition.world;

public class PostList {

    private String profileImage;
    private String userName;
    private String post;
    private String type;
    private String authUserId;
    private String gender;
    private String caption;
    private boolean profileVerified;

    public PostList(String profileImage, String userName, String post, String type, String authUserId, String gender, String caption, boolean profileVerified) {
        this.profileImage = profileImage;
        this.userName = userName;
        this.post = post;
        this.type = type;
        this.authUserId = authUserId;
        this.gender = gender;
        this.caption = caption;
        this.profileVerified = profileVerified;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getPost() {
        return post;
    }

    public String getType() {
        return type;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getGender() {
        return gender;
    }

    public String getCaption() {
        return caption;
    }
    public boolean getProfileVerified() { return profileVerified; }
}
