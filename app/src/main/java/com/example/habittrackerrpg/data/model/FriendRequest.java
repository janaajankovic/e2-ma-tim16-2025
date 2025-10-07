package com.example.habittrackerrpg.data.model;

public class FriendRequest {
    private String requestId;
    private String senderId;
    private String senderUsername;
    private String senderAvatarId;
    private String receiverId;
    private String status;

    public FriendRequest() {}

    public FriendRequest(String senderId, String senderUsername, String senderAvatarId, String receiverId) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderAvatarId = senderAvatarId;
        this.receiverId = receiverId;
        this.status = "PENDING";
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getSenderAvatarId() { return senderAvatarId; }
    public void setSenderAvatarId(String senderAvatarId) { this.senderAvatarId = senderAvatarId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}