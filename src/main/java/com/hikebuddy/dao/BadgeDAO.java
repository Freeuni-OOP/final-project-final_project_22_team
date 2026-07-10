package com.hikebuddy.dao;

import com.hikebuddy.model.Badge;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for badge-related operations.
 * This is the ONLY class allowed to write SQL for the Badge table.
 */
public class BadgeDAO {

    /**
     * Returns all badges earned by a given user.
     */
    public List<Badge> getByUser(int userId) throws SQLException {
        String sql = "SELECT id, user_id, badge_type, earned_at FROM Badge WHERE user_id = ? ORDER BY earned_at ASC";
        List<Badge> badges = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Badge badge = new Badge();
                    badge.setId(rs.getInt("id"));
                    badge.setUserId(rs.getInt("user_id"));
                    badge.setBadgeType(rs.getString("badge_type"));
                    badge.setEarnedAt(rs.getTimestamp("earned_at"));
                    badges.add(badge);
                }
            }
        }
        return badges;
    }

    /**
     * Awards a badge to a user if they don't already have it.
     */
    public void awardIfNotExists(int userId, String badgeType) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Badge WHERE user_id = ? AND badge_type = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, badgeType);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return; // already has badge
            }
        }

        String insertSql = "INSERT INTO Badge (user_id, badge_type) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, badgeType);
            stmt.executeUpdate();
        }
    }
}
