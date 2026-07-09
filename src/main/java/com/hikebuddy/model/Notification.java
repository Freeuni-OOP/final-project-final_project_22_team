package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the Notification table.
 * Plain data carrier — no SQL, no HTTP.
 */
public class Notification {

    private int id;
    private int userId;
    private String type;      // FRIEND_REQUEST, FRIEND_ACCEPTED, HIKE_SUGGESTION
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    // Empty constructor — used by NotificationDAO when mapping ResultSet
    public Notification() {}

    // Convenience constructor for creating new notifications
    public Notification(int userId, String type, String message) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}