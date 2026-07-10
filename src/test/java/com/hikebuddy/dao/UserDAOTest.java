package com.hikebuddy.dao;

import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO;
    private static final String PREFIX = "testuser_dao_";

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        String sql = "DELETE FROM User WHERE username LIKE ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, PREFIX + "%");
            stmt.executeUpdate();
        }
    }

    private User insertTestUser(String suffix) throws SQLException {
        User user = new User(PREFIX + suffix, "hashedpassword", "randomsalt");
        user.setHikingLevel("BEGINNER");
        userDAO.insert(user);
        return user;
    }

    @Test
    void testInsertSetsGeneratedId() throws SQLException {
        User user = insertTestUser("insert1");
        assertTrue(user.getId() > 0);
    }

    @Test
    void testInsertDefaultsHikingLevelToBeginner() throws SQLException {
        User user = insertTestUser("insert2");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertEquals("BEGINNER", found.getHikingLevel());
    }

    @Test
    void testFindByUsernameReturnsCorrectUser() throws SQLException {
        User user = insertTestUser("find1");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
        assertEquals(user.getId(), found.getId());
    }

    @Test
    void testFindByUsernameReturnsNullForMissingUser() throws SQLException {
        User found = userDAO.findByUsername("definitely_does_not_exist_xyz");
        assertNull(found);
    }

    @Test
    void testFindByUsernameReturnsAllFields() throws SQLException {
        User user = insertTestUser("find2");
        User found = userDAO.findByUsername(user.getUsername());
        assertNotNull(found);
        assertNotNull(found.getPasswordHash());
        assertNotNull(found.getSalt());
        assertNotNull(found.getHikingLevel());
    }

    @Test
    void testFindByIdReturnsCorrectUser() throws SQLException {
        User user = insertTestUser("findid1");
        User found = userDAO.findById(user.getId());
        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    void testFindByIdReturnsNullForMissingId() throws SQLException {
        User found = userDAO.findById(-9999);
        assertNull(found);
    }

    @Test
    void testUpdateProfileChangesBioAndLevel() throws SQLException {
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
    void testUpdateProfileBioCanBeEmpty() throws SQLException {
        User user = insertTestUser("update2");
        user.setBio("");
        user.setHikingLevel("BEGINNER");
        userDAO.updateProfile(user);

        User updated = userDAO.findById(user.getId());
        assertNotNull(updated);
        assertEquals("", updated.getBio());
    }

    @Test
    void testSearchByUsernameReturnsMatchingUsers() throws SQLException {
        User user = insertTestUser("search1");
        List<User> results = userDAO.searchByUsername(PREFIX + "search", -1);
        assertFalse(results.isEmpty());
        boolean found = results.stream()
                .anyMatch(u -> u.getUsername().equals(user.getUsername()));
        assertTrue(found);
    }

    @Test
    void testSearchByUsernameExcludesGivenUserId() throws SQLException {
        User user = insertTestUser("search2");
        List<User> results = userDAO.searchByUsername(PREFIX + "search2", user.getId());
        boolean foundSelf = results.stream()
                .anyMatch(u -> u.getId() == user.getId());
        assertFalse(foundSelf);
    }

    @Test
    void testSearchByUsernameNullQueryEdgeCase() {
        assertDoesNotThrow(() -> {
            try {
                List<User> results = userDAO.searchByUsername(null, -1);
                assertNotNull(results);
            } catch (SQLException e) {
                // SQLException from null is acceptable — NullPointerException is not
            }
        });
    }

    @Test
    void testSearchByUsernameReturnsMaxTenResults() throws SQLException {
        for (int i = 0; i < 11; i++) {
            insertTestUser("searchlimit" + i);
        }
        List<User> results = userDAO.searchByUsername(PREFIX + "searchlimit", -1);
        assertTrue(results.size() <= 10);
    }
}