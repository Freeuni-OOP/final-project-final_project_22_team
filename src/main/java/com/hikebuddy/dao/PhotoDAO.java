package com.hikebuddy.dao;

import com.hikebuddy.model.Photo;
import com.hikebuddy.util.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Photo table.
 * This is the ONLY class allowed to write SQL for Photo.
 */
public class PhotoDAO {

    /**
     * Adds a new photo record for a folder.
     */
    public void addPhoto(int folderId, String filePath) throws SQLException {
        String sql = "INSERT INTO Photo (folder_id, file_path) VALUES (?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.setString(2, filePath);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns all photos in a folder, most recent first.
     */
    public List<Photo> getPhotosByFolder(int folderId) throws SQLException {
        String sql = "SELECT id, folder_id, file_path, uploaded_at FROM Photo " +
                "WHERE folder_id = ? ORDER BY uploaded_at DESC";
        List<Photo> photos = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Photo photo = new Photo();
                    photo.setId(rs.getInt("id"));
                    photo.setFolderId(rs.getInt("folder_id"));
                    photo.setFilePath(rs.getString("file_path"));
                    photo.setUploadedAt(rs.getTimestamp("uploaded_at"));
                    photos.add(photo);
                }
            }
        }
        return photos;
    }

    /**
     * Deletes a single photo and returns its file_path so the caller
     * can delete the actual file from disk.
     */
    public String deletePhoto(int photoId) throws SQLException {
        String selectSql = "SELECT file_path FROM Photo WHERE id = ?";
        String filePath = null;
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, photoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    filePath = rs.getString("file_path");
                }
            }
        }

        if (filePath != null) {
            String deleteSql = "DELETE FROM Photo WHERE id = ?";
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, photoId);
                stmt.executeUpdate();
            }
        }

        return filePath;
    }

    /**
     * Deletes all photos in a folder. Used when deleting an entire folder.
     * Does not delete files from disk — caller must do that separately.
     */
    public void deleteAllInFolder(int folderId) throws SQLException {
        String sql = "DELETE FROM Photo WHERE folder_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.executeUpdate();
        }
    }
}