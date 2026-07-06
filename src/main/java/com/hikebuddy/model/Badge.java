package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the Badge table.
 * Plain data class — no SQL, no HTTP.
 */
public class Badge {

    // Badge type constants
    public static final String FIRST_HIKE      = "FIRST_HIKE";
    public static final String TEN_HIKES       = "TEN_HIKES";
    public static final String FIRST_FRIEND    = "FIRST_FRIEND";
    public static final String GEAR_COLLECTOR  = "GEAR_COLLECTOR";

    private int id;
    private int userId;
    private String badgeType;
    private Timestamp earnedAt;

    public Badge() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public Timestamp getEarnedAt() { return earnedAt; }
    public void setEarnedAt(Timestamp earnedAt) { this.earnedAt = earnedAt; }

    /**
     * Returns a human-readable display name for the badge type.
     */
    public String getDisplayName() {
        switch (badgeType) {
            case FIRST_HIKE:     return "First Hike 🥾";
            case TEN_HIKES:      return "10 Hikes 🏔️";
            case FIRST_FRIEND:   return "First Friend 🤝";
            case GEAR_COLLECTOR: return "Gear Collector 🎒";
            default:             return badgeType;
        }
    }
}
