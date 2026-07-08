package com.hikebuddy.model;

/**
 * Represents one row from the HikeRoute table.
 * Plain data class — no SQL, no HTTP.
 * Used by HikeRouteDAO, JourneyServlet (dropdown), and ExploreServlet.
 */
public class HikeRoute {

    private int id;
    private String name;
    private String region;
    private String difficulty; // EASY, MEDIUM, HARD
    private double distance;
    private String description;

    public HikeRoute() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}