package com.hikebuddy.dao;

import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Access Object for availability calendar operations.
 * This is the ONLY class allowed to write SQL for the AvailableDay table.
 */
public class CalendarDAO {

    /**
     * Returns a set of day numbers (1-31) that the user has marked as available
     * for the given year and month.
     */
    public Set<Integer> getAvailableDays(int userId, int year, int month) throws SQLException {
        String sql = "SELECT DAY(available_date) FROM AvailableDay " +
                "WHERE user_id = ? AND YEAR(available_date) = ? AND MONTH(available_date) = ?";
        Set<Integer> days = new HashSet<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    days.add(rs.getInt(1));
                }
            }
        }
        return days;
    }

    /**
     * Toggles a day — inserts if not present, deletes if already present.
     * Uses INSERT IGNORE to add, DELETE to remove.
     */
    public void toggleDay(int userId, LocalDate date) throws SQLException {
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        // Check if the day already exists
        String checkSql = "SELECT COUNT(*) FROM AvailableDay WHERE user_id = ? AND available_date = ?";
        boolean exists;
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, sqlDate);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                exists = rs.getInt(1) > 0;
            }
        }

        if (exists) {
            // Delete it
            String deleteSql = "DELETE FROM AvailableDay WHERE user_id = ? AND available_date = ?";
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, sqlDate);
                stmt.executeUpdate();
            }
        } else {
            // Insert it
            String insertSql = "INSERT INTO AvailableDay (user_id, available_date) VALUES (?, ?)";
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, sqlDate);
                stmt.executeUpdate();
            }
        }
    }
}