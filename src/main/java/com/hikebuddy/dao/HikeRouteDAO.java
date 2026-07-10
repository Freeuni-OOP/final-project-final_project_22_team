package com.hikebuddy.dao;

import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for HikeRoute table.
 * This is the ONLY class allowed to write SQL for HikeRoute.
 */
public class HikeRouteDAO {

    /**
     * Returns all hike routes ordered by name.
     * Used by JourneyServlet to populate the route dropdown.
     */
    public List<HikeRoute> getAll() throws SQLException {
        String sql = "SELECT id, name, region, difficulty, distance, description FROM HikeRoute ORDER BY name";
        List<HikeRoute> routes = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                routes.add(mapRow(rs));
            }
        }
        return routes;
    }

    /**
     * Returns all routes with a given difficulty.
     */
    public List<HikeRoute> getByDifficulty(String difficulty) throws SQLException {
        String sql = "SELECT id, name, region, difficulty, distance, description FROM HikeRoute WHERE difficulty = ?";
        List<HikeRoute> routes = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, difficulty);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        }
        return routes;
    }

    /**
     * Searches routes by name or region using LIKE.
     */
    public List<HikeRoute> searchByNameOrRegion(String query) throws SQLException {
        String sql = "SELECT id, name, region, difficulty, distance, description FROM HikeRoute " +
                "WHERE name LIKE ? OR region LIKE ?";
        String pattern = "%" + query + "%";
        List<HikeRoute> routes = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        }
        return routes;
    }

    /**
     * Returns a single route by id, or null if not found.
     */
    public HikeRoute getById(int id) throws SQLException {
        String sql = "SELECT id, name, region, difficulty, distance, description FROM HikeRoute WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns up to 6 suggested routes based on the user's hiking level,
     * excluding routes already in the user's journey.
     * BEGINNER -> EASY, INTERMEDIATE -> MEDIUM, ADVANCED -> HARD.
     */
    public List<HikeRoute> getSuggested(int userId, String hikingLevel) throws SQLException {
        // Map hiking level to difficulty
        String difficulty;
        switch (hikingLevel) {
            case "INTERMEDIATE": difficulty = "MEDIUM"; break;
            case "ADVANCED":     difficulty = "HARD";   break;
            default:             difficulty = "EASY";   break;
        }

        String sql = "SELECT id, name, region, difficulty, distance, description FROM HikeRoute " +
                "WHERE difficulty = ? " +
                "AND id NOT IN (" +
                "    SELECT hike_route_id FROM JourneyEntry " +
                "    WHERE user_id = ? AND hike_route_id IS NOT NULL" +
                ") " +
                "ORDER BY RAND() LIMIT 6";

        List<HikeRoute> routes = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, difficulty);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        }
        return routes;
    }

    /**
     * Maps a ResultSet row to a HikeRoute object.
     */
    private HikeRoute mapRow(ResultSet rs) throws SQLException {
        HikeRoute route = new HikeRoute();
        route.setId(rs.getInt("id"));
        route.setName(rs.getString("name"));
        route.setRegion(rs.getString("region"));
        route.setDifficulty(rs.getString("difficulty"));
        route.setDistance(rs.getDouble("distance"));
        route.setDescription(rs.getString("description"));
        return route;
    }
}