package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the FriendRequest table.
 * Plain data class — no SQL, no HTTP.
 */
public class FriendRequest {

    private int id;
    private int senderId;
    private int receiverId;
    private String senderUsername; // transient — populated via JOIN, not a DB column
    private String status;         // PENDING, ACCEPTED, DECLINED
    private Timestamp createdAt;

    public FriendRequest() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}