package com.hikebuddy.dao;

import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for JourneyEntry.
 * This is the ONLY class allowed to write SQL for JourneyEntry.
 */
public class JourneyDAO {
    /**
     * Inserts a new journey entry and returns the generated id.
     * If entry.getHikeRouteId() is 0 (unset), stores NULL for hike_route_id
     * instead of 0, since 0 is not a valid HikeRoute id.
     */
    public int addEntry(JourneyEntry entry) throws SQLException {
        String sql = "INSERT INTO JourneyEntry (user_id, hike_route_id, date, distance, difficulty, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entry.getUserId());
            if (entry.getHikeRouteId() > 0) {
                stmt.setInt(2, entry.getHikeRouteId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setDate(3, entry.getDate());
            stmt.setDouble(4, entry.getDistance());
            stmt.setString(5, entry.getDifficulty());
            stmt.setString(6, entry.getStatus());
            stmt.setString(7, entry.getNotes());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id for new JourneyEntry");
    }

    /**
     * Returns entries for a user matching any of the given statuses
     * (e.g. PENDING + COMPLETED for the "planned/completed" section),
     * joined with HikeRoute to populate routeName. Ordered by date descending.
     */
    public List<JourneyEntry> getByUserAndStatuses(int userId, List<String> statuses) throws SQLException {
        if (statuses == null || statuses.isEmpty()) {
            return new ArrayList<>();
        }
        String placeholders = String.join(",", statuses.stream().map(s -> "?").toArray(String[]::new));
        String sql = "SELECT je.*, hr.name AS route_name " +
                "FROM JourneyEntry je " +
                "LEFT JOIN HikeRoute hr ON je.hike_route_id = hr.id " +
                "WHERE je.user_id = ? AND je.status IN (" + placeholders + ") " +
                "ORDER BY je.date DESC";
        List<JourneyEntry> results = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;
            stmt.setInt(idx++, userId);
            for (String status : statuses) {
                stmt.setString(idx++, status);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Returns all WISHLIST entries for a user, joined with HikeRoute for routeName.
     */
    public List<JourneyEntry> getWishlist(int userId) throws SQLException {
        String sql = "SELECT je.*, hr.name AS route_name " +
                "FROM JourneyEntry je " +
                "LEFT JOIN HikeRoute hr ON je.hike_route_id = hr.id " +
                "WHERE je.user_id = ? AND je.status = 'WISHLIST' " +
                "ORDER BY je.date DESC";
        List<JourneyEntry> results = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Updates the status of a single entry (e.g. PENDING -> COMPLETED,
     * or WISHLIST -> PENDING when moved to planned).
     * Scoped to userId so a user cannot update another user's entry
     * by guessing/manipulating entryId.
     */
    public boolean updateStatus(int entryId, String status, int userId) throws SQLException {
        String sql = "UPDATE JourneyEntry SET status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, entryId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a single journey entry.
     * Scoped to userId so a user cannot delete another user's entry
     * by guessing/manipulating entryId.
     */
    public boolean deleteEntry(int entryId, int userId) throws SQLException {
        String sql = "DELETE FROM JourneyEntry WHERE id = ? AND user_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entryId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Returns the number of COMPLETED entries for a user (for profile stats).
     */
    public int getCountByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM JourneyEntry WHERE user_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Returns the last `limit` completed entries for a user, joined with
     * HikeRoute for routeName. Used by the Explore page (Epic 7/8).
     */
    public List<JourneyEntry> getRecentCompleted(int userId, int limit) throws SQLException {
        String sql = "SELECT je.*, hr.name AS route_name " +
                "FROM JourneyEntry je " +
                "JOIN HikeRoute hr ON je.hike_route_id = hr.id " +
                "WHERE je.user_id = ? AND je.status = 'COMPLETED' " +
                "ORDER BY je.date DESC LIMIT ?";
        List<JourneyEntry> results = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * For each friend userId in the given list, returns their last `limitPerUser`
     * completed entries with route name. Used by ExploreServlet (Epic 8) to show
     * friends' recent hikes. Returns an empty list if userIds is empty.
     */
    public List<JourneyEntry> getRecentCompletedForUsers(List<Integer> userIds, int limitPerUser) throws SQLException {
        List<JourneyEntry> allResults = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            return allResults;
        }
        for (int userId : userIds) {
            allResults.addAll(getRecentCompleted(userId, limitPerUser));
        }
        return allResults;
    }

    /**
     * Maps a ResultSet row (from a query joining JourneyEntry with HikeRoute)
     * to a JourneyEntry object, including the transient routeName field.
     */
    private JourneyEntry mapRow(ResultSet rs) throws SQLException {
        JourneyEntry entry = new JourneyEntry();
        entry.setId(rs.getInt("id"));
        entry.setUserId(rs.getInt("user_id"));
        entry.setHikeRouteId(rs.getInt("hike_route_id")); // 0 if column was NULL
        entry.setRouteName(rs.getString("route_name"));   // null if no matching route
        entry.setDate(rs.getDate("date"));
        entry.setDistance(rs.getDouble("distance"));
        entry.setDifficulty(rs.getString("difficulty"));
        entry.setStatus(rs.getString("status"));
        entry.setNotes(rs.getString("notes"));
        return entry;
    }

    public JourneyEntry getById(int entryId, int userId) throws SQLException {
        String sql = "SELECT je.*, hr.name AS route_name " +
                "FROM JourneyEntry je " +
                "LEFT JOIN HikeRoute hr ON je.hike_route_id = hr.id " +
                "WHERE je.id = ? AND je.user_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entryId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
}