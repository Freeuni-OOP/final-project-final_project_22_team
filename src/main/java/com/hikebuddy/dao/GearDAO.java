package com.hikebuddy.dao;

import com.hikebuddy.model.Gear;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for all gear-related operations.
 * This is the ONLY class allowed to write SQL for the Gear table.
 */
public class GearDAO {

    /**
     * Returns all gear items for a given user, ordered by id.
     */
    public List<Gear> getByUser(int userId) throws SQLException {
        String sql = "SELECT id, user_id, name, is_checked FROM Gear WHERE user_id = ? ORDER BY id ASC";
        List<Gear> gearList = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Gear gear = new Gear();
                    gear.setId(rs.getInt("id"));
                    gear.setUserId(rs.getInt("user_id"));
                    gear.setName(rs.getString("name"));
                    gear.setChecked(rs.getBoolean("is_checked"));
                    gearList.add(gear);
                }
            }
        }
        return gearList;
    }

    /**
     * Returns the number of gear items a user has, without fetching the rows.
     */
    public int getCountByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Gear WHERE user_id = ?";
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
     * Adds a new unchecked gear item for a user.
     */
    public void addGear(int userId, String name) throws SQLException {
        String sql = "INSERT INTO Gear (user_id, name) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    /**
     * Toggles the checked state of a gear item.
     * Flips is_checked to the opposite of its current state.
     */
    public void toggleCheck(int gearId, boolean currentState) throws SQLException {
        String sql = "UPDATE Gear SET is_checked = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, !currentState);
            stmt.setInt(2, gearId);
            stmt.executeUpdate();
        }
    }
}