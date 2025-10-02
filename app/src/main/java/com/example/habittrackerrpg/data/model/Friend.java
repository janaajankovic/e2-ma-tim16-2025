package com.example.habittrackerrpg.data.model;

public class Friend {
    private String userId;
    private String username;
    private String avatarId;

    public Friend() {}

    public Friend(String userId, String username, String avatarId) {
        this.userId = userId;
        this.username = username;
        this.avatarId = avatarId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }
}