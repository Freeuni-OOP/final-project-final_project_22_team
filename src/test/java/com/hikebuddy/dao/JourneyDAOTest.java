package com.hikebuddy.dao;

import com.hikebuddy.model.JourneyEntry;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for JourneyDAO. Runs against the real MySQL database
 * configured in db.properties (same one the app uses locally) — JourneyDAO
 * has no mocking seam, it calls DBHelper.getConnection() directly.
 * Each test builds its own User/HikeRoute fixtures and deletes them in
 * tearDown so runs don't leak rows into the shared dev database.
 */
class JourneyDAOTest {

    private final JourneyDAO journeyDAO = new JourneyDAO();
    private final UserDAO userDAO = new UserDAO();

    private int userId;
    private int otherUserId;
    private int hikeRouteId;

    @BeforeEach
    void setUp() throws SQLException {
        userId = insertTestUser("journeydao_user");
        otherUserId = insertTestUser("journeydao_other");
        hikeRouteId = insertTestHikeRoute();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM JourneyEntry WHERE user_id IN (" + userId + ", " + otherUserId + ")");
            stmt.executeUpdate("DELETE FROM HikeRoute WHERE id = " + hikeRouteId);
            stmt.executeUpdate("DELETE FROM User WHERE id IN (" + userId + ", " + otherUserId + ")");
        }
    }

    @Test
    void addEntryReturnsGeneratedId() throws SQLException {
        int id = journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING"));

        assertTrue(id > 0);
        JourneyEntry saved = journeyDAO.getById(id, userId);
        assertNotNull(saved);
        assertEquals(hikeRouteId, saved.getHikeRouteId());
    }

    @Test
    void addEntryStoresNullHikeRouteIdWhenZero() throws SQLException {
        int id = journeyDAO.addEntry(newEntry(userId, 0, "WISHLIST"));

        JourneyEntry saved = journeyDAO.getById(id, userId);
        assertNotNull(saved);
        assertEquals(0, saved.getHikeRouteId()); // 0 means the column was NULL
        assertNull(saved.getRouteName());
    }

    @Test
    void getByUserAndStatusesFiltersByMultipleStatusesAndOrdersByDateDescending() throws SQLException {
        int pendingId = journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "PENDING", "2026-01-10"));
        int completedId = journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "COMPLETED", "2026-03-10"));
        journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "WISHLIST", "2026-02-10")); // excluded status

        List<JourneyEntry> results = journeyDAO.getByUserAndStatuses(userId, Arrays.asList("PENDING", "COMPLETED"));

        assertEquals(2, results.size());
        assertEquals(completedId, results.get(0).getId()); // newest date first
        assertEquals(pendingId, results.get(1).getId());
    }

    @Test
    void getWishlistReturnsOnlyWishlistEntriesForThatUser() throws SQLException {
        int wishlistId = journeyDAO.addEntry(newEntry(userId, hikeRouteId, "WISHLIST"));
        journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING"));
        journeyDAO.addEntry(newEntry(otherUserId, hikeRouteId, "WISHLIST")); // different user

        List<JourneyEntry> results = journeyDAO.getWishlist(userId);

        assertEquals(1, results.size());
        assertEquals(wishlistId, results.get(0).getId());
    }

    @Test
    void getRecentCompletedReturnsOnlyCompletedEntriesUpToLimitNewestFirst() throws SQLException {
        journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "COMPLETED", "2026-01-01"));
        int mostRecentId = journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "COMPLETED", "2026-04-01"));
        journeyDAO.addEntry(newEntryWithDate(userId, hikeRouteId, "COMPLETED", "2026-03-01"));
        journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING")); // excluded status

        List<JourneyEntry> results = journeyDAO.getRecentCompleted(userId, 2);

        assertEquals(2, results.size());
        assertEquals(mostRecentId, results.get(0).getId());
    }

    @Test
    void updateStatusWithMismatchedUserIdAffectsZeroRowsAndLeavesEntryUnchanged() throws SQLException {
        int entryId = journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING"));

        boolean updated = journeyDAO.updateStatus(entryId, "COMPLETED", otherUserId);

        assertFalse(updated);
        assertEquals("PENDING", journeyDAO.getById(entryId, userId).getStatus());
    }

    @Test
    void deleteEntryWithMismatchedUserIdAffectsZeroRowsAndLeavesEntryIntact() throws SQLException {
        int entryId = journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING"));

        boolean deleted = journeyDAO.deleteEntry(entryId, otherUserId);

        assertFalse(deleted);
        assertNotNull(journeyDAO.getById(entryId, userId));
    }

    @Test
    void getByIdReturnsEntryForOwningUserAndNullForAnotherUser() throws SQLException {
        int entryId = journeyDAO.addEntry(newEntry(userId, hikeRouteId, "PENDING"));

        assertNotNull(journeyDAO.getById(entryId, userId));
        assertNull(journeyDAO.getById(entryId, otherUserId));
    }

    // ---------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------

    private JourneyEntry newEntry(int userId, int hikeRouteId, String status) {
        return newEntryWithDate(userId, hikeRouteId, status, "2026-01-01");
    }

    private JourneyEntry newEntryWithDate(int userId, int hikeRouteId, String status, String isoDate) {
        JourneyEntry entry = new JourneyEntry();
        entry.setUserId(userId);
        entry.setHikeRouteId(hikeRouteId);
        entry.setDate(Date.valueOf(isoDate));
        entry.setDistance(5.0);
        entry.setDifficulty("EASY");
        entry.setStatus(status);
        entry.setNotes("test entry");
        return entry;
    }

    private int insertTestUser(String usernamePrefix) throws SQLException {
        User user = new User(usernamePrefix + "_" + System.nanoTime(), "hash", "salt");
        userDAO.insert(user);
        return user.getId();
    }

    private int insertTestHikeRoute() throws SQLException {
        String sql = "INSERT INTO HikeRoute (name, region, difficulty, distance, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "JourneyDAOTest Route " + System.nanoTime());
            stmt.setString(2, "Test Region");
            stmt.setString(3, "EASY");
            stmt.setDouble(4, 5.0);
            stmt.setString(5, "Fixture route for JourneyDAOTest");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }
}
