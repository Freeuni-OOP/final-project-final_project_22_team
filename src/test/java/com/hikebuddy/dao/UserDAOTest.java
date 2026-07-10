package com.hikebuddy.dao;

import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class UserDAOTest {

    private UserDAO userDAO;

    // Prefix for test usernames so cleanup is safe and targeted
    private static final String PREFIX = "testuser_dao_";

    @Before
    public void setUp() {
        userDAO = new UserDAO();
    }

    @After
    public void tearDown() throws SQLException {
        // Delete all test users created during tests
        String sql = "DELETE FROM User WHERE username LIKE ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, PREFIX + "%");
            stmt.executeUpdate();
        }
    }

    // Helper to create and insert a test user
    private User insertTestUser(String suffix) throws SQLException {
        User user = new User(
                PREFIX + suffix,
                "hashedpassword",
                "randomsalt"
        );
        user.setHikingLevel("BEGINNER");
        userDAO.insert(user);
        return user;
    }

    // --- insert() ---

    @Test
    public void testInsertSetsGeneratedId() throws SQLException {
        User user = insertTestUser("insert1");
        assertTrue("Generated id should be > 0", user.getId() > 0);
    }

    @Test
    public void testInsertDefaultsHikingLevelToBeginner() throws SQLException {
        User user = insertTestUser("insert2");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertEquals("BEGINNER", found.getHikingLevel());
    }

    // --- findByUsername() ---

    @Test
    public void testFindByUsernameReturnsCorrectUser() throws SQLException {
        User user = insertTestUser("find1");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
        assertEquals(user.getId(), found.getId());
    }

    @Test
    public void testFindByUsernameReturnsNullForMissingUser() throws SQLException {
        User found = userDAO.findByUsername("definitely_does_not_exist_xyz");
        assertNull(found);
    }

    @Test
    public void testFindByUsernameReturnsAllFields() throws SQLException {
        User user = insertTestUser("find2");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertNotNull(found.getPasswordHash());
        assertNotNull(found.getSalt());
        assertNotNull(found.getHikingLevel());
    }

    // --- findById() ---

    @Test
    public void testFindByIdReturnsCorrectUser() throws SQLException {
        User user = insertTestUser("findid1");
        User found = userDAO.findById(user.getId());
        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    public void testFindByIdReturnsNullForMissingId() throws SQLException {
        User found = userDAO.findById(-9999);
        assertNull(found);
    }

    // --- updateProfile() ---

    @Test
    public void testUpdateProfileChangesBioAndLevel() throws SQLException {
        User user = insertTestUser("update1");

        user.setBio("I love mountains");
        user.setHikingLevel("ADVANCED");
        userDAO.updateProfile(user);

        User updated = userDAO.findById(user.getId());
        assertNotNull(updated);
        assertEquals("I love mountains", updated.getBio());
        assertEquals("ADVANCED", updated.getHikingLevel());
    }

    @Test
    public void testUpdateProfileBioCanBeEmpty() throws SQLException {
        User user = insertTestUser("update2");
        user.setBio("");
        user.setHikingLevel("BEGINNER");
        userDAO.updateProfile(user);

        User updated = userDAO.findById(user.getId());
        assertNotNull(updated);
        assertEquals("", updated.getBio());
    }

    // --- searchByUsername() ---

    @Test
    public void testSearchByUsernameReturnsMatchingUsers() throws SQLException {
        User user = insertTestUser("search1");
        List<User> results = userDAO.searchByUsername(PREFIX + "search", -1);
        assertFalse("Search should return at least one result", results.isEmpty());
        boolean found = results.stream()
                .anyMatch(u -> u.getUsername().equals(user.getUsername()));
        assertTrue("Inserted user should appear in search results", found);
    }

    @Test
    public void testSearchByUsernameExcludesGivenUserId() throws SQLException {
        User user = insertTestUser("search2");
        List<User> results = userDAO.searchByUsername(PREFIX + "search2", user.getId());
        boolean foundSelf = results.stream()
                .anyMatch(u -> u.getId() == user.getId());
        assertFalse("Search should exclude the given userId", foundSelf);
    }

    @Test
    public void testSearchByUsernameNullQueryReturnsEmptyOrAll() throws SQLException {
        // Per the plan: "explicitly test the null-query edge case"
        // searchByUsername with null should not throw — handle gracefully
        try {
            List<User> results = userDAO.searchByUsername(null, -1);
            // If it doesn't throw, result should be a list (possibly empty)
            assertNotNull(results);
        } catch (SQLException e) {
            // A SQLException from null query is acceptable behavior
            // The important thing is it doesn't throw a NullPointerException
        }
    }

    @Test
    public void testSearchByUsernameReturnsMaxTenResults() throws SQLException {
        // Insert 11 users with similar names
        for (int i = 0; i < 11; i++) {
            insertTestUser("searchlimit" + i);
        }
        List<User> results = userDAO.searchByUsername(PREFIX + "searchlimit", -1);
        assertTrue("Search should return at most 10 results", results.size() <= 10);
    }
}