package com.hikebuddy.model;

import java.sql.Date;

/**
 * Represents one row from the JourneyEntry table.
 * Plain data class — no SQL, no HTTP.
 * routeName is transient — populated via JOIN with HikeRoute, not a DB column.
 */
public class JourneyEntry {
    private int id;
    private int userId;
    private int hikeRouteId;
    private String routeName; // transient — from JOIN, no DB column
    private Date date;
    private double distance;
    private String difficulty; // EASY, MEDIUM, HARD
    private String status;     // WISHLIST, PENDING, COMPLETED
    private String notes;

    public JourneyEntry() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getHikeRouteId() { return hikeRouteId; }
    public void setHikeRouteId(int hikeRouteId) { this.hikeRouteId = hikeRouteId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}