package com.hikebuddy.dao;

import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.model.StoryFolder;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for StoryFolderDAO. Runs against the real MySQL database
 * configured in db.properties — StoryFolderDAO has no mocking seam, it calls
 * DBHelper.getConnection() directly. Each test builds its own User fixture
 * (and JourneyEntry/Photo rows where needed) and deletes everything in
 * tearDown so runs don't leak rows into the shared dev database.
 */
class StoryFolderDAOTest {

    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();
    private final JourneyDAO journeyDAO = new JourneyDAO();
    private final UserDAO userDAO = new UserDAO();

    private int userId;

    @BeforeEach
    void setUp() throws SQLException {
        User user = new User("storyfolderdao_user_" + System.nanoTime(), "hash", "salt");
        userDAO.insert(user);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Photo WHERE folder_id IN (SELECT id FROM StoryFolder WHERE user_id = " + userId + ")");
            stmt.executeUpdate("DELETE FROM StoryFolder WHERE user_id = " + userId);
            stmt.executeUpdate("DELETE FROM JourneyEntry WHERE user_id = " + userId);
            stmt.executeUpdate("DELETE FROM User WHERE id = " + userId);
        }
    }

    @Test
    void createFolderWithJourneyEntryIdLinksTheEntry() throws SQLException {
        int entryId = insertJourneyEntry();

        int folderId = storyFolderDAO.createFolder(userId, entryId, "Kazbegi Trip");

        assertTrue(folderId > 0);
        StoryFolder saved = findOne(folderId);
        assertEquals(entryId, saved.getJourneyEntryId());
        assertEquals("Kazbegi Trip", saved.getName());
    }

    @Test
    void createFolderWithNullJourneyEntryIdLeavesItNull() throws SQLException {
        int folderId = storyFolderDAO.createFolder(userId, null, "Manual Folder");

        assertNull(findOne(folderId).getJourneyEntryId());
    }

    @Test
    void getFoldersByUserPopulatesThumbnailFromMostRecentPhoto() throws SQLException {
        int folderId = storyFolderDAO.createFolder(userId, null, "With Photos");
        insertPhoto(folderId, "/uploads/old.jpg", "2026-01-01 10:00:00");
        insertPhoto(folderId, "/uploads/new.jpg", "2026-01-02 10:00:00");

        List<StoryFolder> folders = storyFolderDAO.getFoldersByUser(userId);

        assertEquals(1, folders.size());
        assertEquals("/uploads/new.jpg", folders.get(0).getThumbnailPath());
    }

    @Test
    void getFoldersByUserLeavesThumbnailNullWhenFolderHasNoPhotos() throws SQLException {
        storyFolderDAO.createFolder(userId, null, "No Photos");

        List<StoryFolder> folders = storyFolderDAO.getFoldersByUser(userId);

        assertEquals(1, folders.size());
        assertNull(folders.get(0).getThumbnailPath());
    }

    @Test
    void deleteFolderWithPhotosRemovesBothPhotoRowsAndTheFolderRow() throws SQLException {
        int folderId = storyFolderDAO.createFolder(userId, null, "To Delete");
        insertPhoto(folderId, "/uploads/a.jpg", "2026-01-01 10:00:00");
        insertPhoto(folderId, "/uploads/b.jpg", "2026-01-01 11:00:00");

        storyFolderDAO.deleteFolderWithPhotos(folderId);

        assertEquals(0, countPhotos(folderId));
        assertEquals(0, countFolders(folderId));
    }

    // ---------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------

    private int insertJourneyEntry() throws SQLException {
        JourneyEntry entry = new JourneyEntry();
        entry.setUserId(userId);
        entry.setHikeRouteId(0); // NULL — no HikeRoute dependency needed for this test
        entry.setDate(Date.valueOf("2026-01-01"));
        entry.setDistance(5.0);
        entry.setDifficulty("EASY");
        entry.setStatus("COMPLETED");
        entry.setNotes("test entry");
        return journeyDAO.addEntry(entry);
    }

    private void insertPhoto(int folderId, String filePath, String uploadedAt) throws SQLException {
        String sql = "INSERT INTO Photo (folder_id, file_path, uploaded_at) VALUES (?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.setString(2, filePath);
            stmt.setTimestamp(3, Timestamp.valueOf(uploadedAt));
            stmt.executeUpdate();
        }
    }

    private StoryFolder findOne(int folderId) throws SQLException {
        return storyFolderDAO.getFoldersByUser(userId).stream()
                .filter(f -> f.getId() == folderId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Folder " + folderId + " not found for user " + userId));
    }

    private int countPhotos(int folderId) throws SQLException {
        return countWhere("SELECT COUNT(*) FROM Photo WHERE folder_id = ?", folderId);
    }

    private int countFolders(int folderId) throws SQLException {
        return countWhere("SELECT COUNT(*) FROM StoryFolder WHERE id = ?", folderId);
    }

    private int countWhere(String sql, int id) throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
