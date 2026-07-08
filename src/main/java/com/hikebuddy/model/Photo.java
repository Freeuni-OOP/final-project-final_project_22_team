package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the Photo table.
 * Plain data class — no SQL, no HTTP.
 */
public class Photo {

    private int id;
    private int folderId;
    private String filePath;
    private Timestamp uploadedAt;

    public Photo() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFolderId() { return folderId; }
    public void setFolderId(int folderId) { this.folderId = folderId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}