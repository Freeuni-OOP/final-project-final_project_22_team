package com.hikebuddy.dao;
import java.util.List;
import java.util.ArrayList;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for the User table.
 * This is the ONLY class allowed to write raw SQL for users.
 * Servlets call these methods instead of touching the database directly.
 */
public class UserDAO {

    public void insert(User u) throws SQLException {
        String sql = "INSERT INTO User (username, password_hash, salt, hiking_level) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, u.getUsername());
            stmt.setString(2, u.getPasswordHash());
            stmt.setString(3, u.getSalt());
            stmt.setString(4, u.getHikingLevel());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    u.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, hiking_level, bio, created_at " +
                "FROM User WHERE username = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<User> searchByUsername(String query, int excludeUserId) throws SQLException {
        String sql = "SELECT id, username FROM User WHERE username LIKE ? AND id != ? LIMIT 10";
        List<User> results = new ArrayList<>();

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + query + "%");
            stmt.setInt(2, excludeUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    results.add(u);
                }
            }
        }

        return results;
    }
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setHikingLevel(rs.getString("hiking_level"));
        user.setBio(rs.getString("bio"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }

    public void updateProfile(User u) throws SQLException {
        String sql = "UPDATE User SET bio=?, hiking_level=? WHERE id=?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getBio());
            stmt.setString(2, u.getHikingLevel());
            stmt.setInt(3, u.getId());
            stmt.executeUpdate();
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt, hiking_level, bio, created_at " +
                "FROM User WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                } else {
                    return null;
                }
            }
        }
    }
}