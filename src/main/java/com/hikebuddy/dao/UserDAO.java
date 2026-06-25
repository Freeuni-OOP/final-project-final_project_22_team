package com.hikebuddy.dao;

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

    /**
     * Inserts a new user into the database.
     * Retrieves the auto-generated id and sets it back on the User object,
     * so the caller (RegisterServlet) immediately has a fully-populated User
     * without needing a separate SELECT.
     * Throws SQLException - the caller decides how to handle/display it.
     */
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

    /**
     * Finds a user by their username.
     * Returns null if no user with that username exists.
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM User WHERE username = ?";

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

    /**
     * Helper method: converts one row of a ResultSet into a User object.
     */
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
}