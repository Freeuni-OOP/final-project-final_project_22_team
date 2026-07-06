package com.hikebuddy.model;

/**
 * Represents one row from the Gear table.
 * Plain data class — no SQL, no HTTP.
 */
public class Gear {

    private int id;
    private int userId;
    private String name;
    private boolean isChecked;

    public Gear() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}
