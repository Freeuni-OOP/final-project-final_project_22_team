package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the StoryFolder table.
 * Plain data class — no SQL, no HTTP.
 * thumbnailPath is transient — populated via JOIN with Photo, not a DB column.
 */
public class StoryFolder {

    private int id;
    private int userId;
    private Integer journeyEntryId; // nullable — null for manually created folders
    private String name;
    private String description;
    private Timestamp createdAt;
    private String thumbnailPath; // transient — first photo's file_path, for display

    public StoryFolder() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getJourneyEntryId() { return journeyEntryId; }
    public void setJourneyEntryId(Integer journeyEntryId) { this.journeyEntryId = journeyEntryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
}