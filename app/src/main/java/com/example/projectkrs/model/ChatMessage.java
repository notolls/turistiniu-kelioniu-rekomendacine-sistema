package com.example.projectkrs.model;

import com.google.firebase.Timestamp;

public class ChatMessage {

    private String text;
    private String userEmail;
    private String userId;
    private Timestamp timestamp;

    public ChatMessage() {}

    public ChatMessage(String text, String userEmail, String userId, Timestamp timestamp) {
        this.text = text;
        this.userEmail = userEmail;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getText() { return text; }
    public String getUserEmail() { return userEmail; }
    public String getUserId() { return userId; }
    public Timestamp getTimestamp() { return timestamp; }
}