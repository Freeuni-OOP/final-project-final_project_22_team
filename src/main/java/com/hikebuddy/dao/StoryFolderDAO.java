package com.hikebuddy.dao;

import com.hikebuddy.model.StoryFolder;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for StoryFolder table.
 * This is the ONLY class allowed to write SQL for StoryFolder.
 */
public class StoryFolderDAO {

    /**
     * Creates a new folder and returns the generated id.
     * journeyEntryId can be null for manually created folders.
     */
    public int createFolder(int userId, Integer journeyEntryId, String name) throws SQLException {
        String sql = "INSERT INTO StoryFolder (user_id, journey_entry_id, name) VALUES (?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            if (journeyEntryId != null) {
                stmt.setInt(2, journeyEntryId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, name);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated id for new StoryFolder");
    }

    /**
     * Returns all folders for a user, with a thumbnail (first photo's file_path).
     * Uses a subquery to get the most recent photo per folder.
     */
    public List<StoryFolder> getFoldersByUser(int userId) throws SQLException {
        String sql = "SELECT sf.id, sf.user_id, sf.journey_entry_id, sf.name, sf.description, sf.created_at, " +
                "(SELECT p.file_path FROM Photo p WHERE p.folder_id = sf.id ORDER BY p.uploaded_at DESC LIMIT 1) AS thumbnail_path " +
                "FROM StoryFolder sf " +
                "WHERE sf.user_id = ? " +
                "ORDER BY sf.created_at DESC";
        List<StoryFolder> folders = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StoryFolder folder = new StoryFolder();
                    folder.setId(rs.getInt("id"));
                    folder.setUserId(rs.getInt("user_id"));
                    int journeyEntryId = rs.getInt("journey_entry_id");
                    folder.setJourneyEntryId(rs.wasNull() ? null : journeyEntryId);
                    folder.setName(rs.getString("name"));
                    folder.setDescription(rs.getString("description"));
                    folder.setCreatedAt(rs.getTimestamp("created_at"));
                    folder.setThumbnailPath(rs.getString("thumbnail_path"));
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    /**
     * Deletes a folder. Caller must delete associated photos first
     * (both DB rows and disk files) before calling this.
     */
    public void deleteFolder(int folderId) throws SQLException {
        String sql = "DELETE FROM StoryFolder WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.executeUpdate();
        }
    }
}