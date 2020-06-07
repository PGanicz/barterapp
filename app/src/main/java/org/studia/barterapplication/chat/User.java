package org.studia.barterapplication.chat;

public class User {
    private String uid;
    private String username;
    private String imageUrl;

    public String getStatus() {
        return status;
    }

    private String status;

    public User(String uid, String username, String imageUrl) {
        this.uid = uid;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
