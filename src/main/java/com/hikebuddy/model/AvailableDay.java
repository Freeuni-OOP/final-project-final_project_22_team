package com.hikebuddy.model;

import java.sql.Date;

/**
 * Represents one row from the AvailableDay table.
 * Plain data class — no SQL, no HTTP.
 */
public class AvailableDay {

    private int id;
    private int userId;
    private Date availableDate;

    public AvailableDay() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getAvailableDate() { return availableDate; }
    public void setAvailableDate(Date availableDate) { this.availableDate = availableDate; }
}
