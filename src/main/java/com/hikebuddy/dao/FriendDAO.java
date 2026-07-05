package com.hikebuddy.dao;

import com.hikebuddy.model.FriendRequest;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for all friend-related operations.
 * Handles FriendRequest and Friendship tables.
 * This is the ONLY class allowed to write SQL for friends.
 */
public class FriendDAO {

    // FRIEND REQUESTS

    /**
     * Sends a friend request from sender to receiver.
     * Inserts a new row into FriendRequest with status PENDING.
     */
    public void sendRequest(int senderId, int receiverId) throws SQLException {
        String sql = "INSERT INTO FriendRequest (sender_id, receiver_id) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns all PENDING friend requests where the given user is the receiver.
     * Joins with User table to get the sender's username.
     */
    public List<FriendRequest> getIncomingRequests(int userId) throws SQLException {
        String sql = "SELECT fr.id, fr.sender_id, fr.receiver_id, fr.status, fr.created_at, " +
                "u.username AS sender_username " +
                "FROM FriendRequest fr " +
                "JOIN User u ON fr.sender_id = u.id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING' " +
                "ORDER BY fr.created_at DESC";
        List<FriendRequest> requests = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FriendRequest fr = new FriendRequest();
                    fr.setId(rs.getInt("id"));
                    fr.setSenderId(rs.getInt("sender_id"));
                    fr.setReceiverId(rs.getInt("receiver_id"));
                    fr.setStatus(rs.getString("status"));
                    fr.setCreatedAt(rs.getTimestamp("created_at"));
                    fr.setSenderUsername(rs.getString("sender_username"));
                    requests.add(fr);
                }
            }
        }
        return requests;
    }

    /**
     * Accepts a friend request.
     * Updates FriendRequest status to ACCEPTED and inserts into Friendship table.
     * Uses a transaction — both must succeed or neither happens.
     */
    public void acceptRequest(int requestId, int senderId, int receiverId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1. Update request status
            String updateSql = "UPDATE FriendRequest SET status = 'ACCEPTED' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, requestId);
                stmt.executeUpdate();
            }

            // 2. Insert into Friendship (store smaller id first to avoid duplicates)
            int id1 = Math.min(senderId, receiverId);
            int id2 = Math.max(senderId, receiverId);
            String insertSql = "INSERT INTO Friendship (user_id_1, user_id_2) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, id1);
                stmt.setInt(2, id2);
                stmt.executeUpdate();
            }

            conn.commit(); // commit transaction
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // rollback on failure
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Declines a friend request.
     * Updates FriendRequest status to DECLINED.
     */
    public void declineRequest(int requestId) throws SQLException {
        String sql = "UPDATE FriendRequest SET status = 'DECLINED' WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            stmt.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // FRIENDSHIPS


    /**
     * Returns all friends of a given user.
     * Since friendship is stored as (min_id, max_id), we check both columns.
     * Joins with User to get friend's username.
     */
    public List<User> getFriends(int userId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.hiking_level, u.bio " +
                "FROM Friendship f " +
                "JOIN User u ON (f.user_id_1 = u.id OR f.user_id_2 = u.id) " +
                "WHERE (f.user_id_1 = ? OR f.user_id_2 = ?) " +
                "AND u.id != ? " +
                "ORDER BY u.username ASC";
        List<User> friends = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setHikingLevel(rs.getString("hiking_level"));
                    u.setBio(rs.getString("bio"));
                    friends.add(u);
                }
            }
        }
        return friends;
    }

    /**
     * Removes a friendship between two users.
     * Deletes from Friendship table checking both orderings.
     */
    public void removeFriend(int userId, int friendId) throws SQLException {
        String sql = "DELETE FROM Friendship WHERE " +
                "(user_id_1 = ? AND user_id_2 = ?) OR " +
                "(user_id_1 = ? AND user_id_2 = ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.min(userId, friendId));
            stmt.setInt(2, Math.max(userId, friendId));
            stmt.setInt(3, Math.max(userId, friendId));
            stmt.setInt(4, Math.min(userId, friendId));
            stmt.executeUpdate();
        }
    }

    /**
     * Returns the number of friends a user has.
     * Used by ProfileServlet to show friend count on profile page.
     */
    public int getFriendCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Friendship " +
                "WHERE user_id_1 = ? OR user_id_2 = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Checks if two users are already friends.
     */
    public boolean areFriends(int userId1, int userId2) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Friendship " +
                "WHERE user_id_1 = ? AND user_id_2 = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.min(userId1, userId2));
            stmt.setInt(2, Math.max(userId1, userId2));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Checks if a pending friend request already exists between two users.
     * Checks both directions to prevent duplicate requests.
     */
    public boolean hasPendingRequest(int senderId, int receiverId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM FriendRequest " +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR " +
                "(sender_id = ? AND receiver_id = ?)) " +
                "AND status = 'PENDING'";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}