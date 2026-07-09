package com.hikebuddy.dao;

import com.hikebuddy.model.Notification;
import com.hikebuddy.util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Notification table.
 * All SQL for notifications lives here.
 */
public class NotificationDAO {

    /**
     * Creates a new notification for a user.
     * Called by FriendsServlet when a friend request is sent or accepted.
     */
    public void create(int userId, String type, String message) throws SQLException {
        String sql = "INSERT INTO Notification (user_id, type, message) VALUES (?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setString(3, message);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns all unread notifications for a user, newest first.
     * Used to display the notification list when user clicks the bell icon.
     */
    public List<Notification> getUnread(int userId) throws SQLException {
        String sql = "SELECT id, user_id, type, message, is_read, created_at " +
                "FROM Notification " +
                "WHERE user_id = ? AND is_read = FALSE " +
                "ORDER BY created_at DESC";

        List<Notification> list = new ArrayList<>();

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToNotification(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns count of unread notifications for a user.
     * Used to show the red badge number in the nav bar.
     */
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Notification WHERE user_id = ? AND is_read = FALSE";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Marks all notifications as read for a user.
     * Called when user clicks the bell icon to view notifications.
     */
    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE Notification SET is_read = TRUE WHERE user_id = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Marks a single notification as read.
     */
    public void markRead(int notifId) throws SQLException {
        String sql = "UPDATE Notification SET is_read = TRUE WHERE id = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notifId);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps one ResultSet row to a Notification object.
     */
    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}