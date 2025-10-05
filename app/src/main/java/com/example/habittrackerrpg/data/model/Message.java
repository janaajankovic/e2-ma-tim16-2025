package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String text;
    private String senderId;
    private String senderUsername;

    @ServerTimestamp
    private Date timestamp;

    public Message() {}

    public Message(String text, String senderId, String senderUsername) {
        this.text = text;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
    }

    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public Date getTimestamp() { return timestamp; }
}