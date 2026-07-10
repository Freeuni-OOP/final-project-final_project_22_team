package com.hikebuddy.dao;

import com.hikebuddy.model.Gear;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GearDAO. Runs against the real MySQL database
 * configured in db.properties — GearDAO has no mocking seam, it calls
 * DBHelper.getConnection() directly. Each test builds its own User fixture
 * and deletes all Gear/User rows in tearDown so runs don't leak data
 * into the shared dev database.
 */
class GearDAOTest {

    private final GearDAO gearDAO = new GearDAO();
    private final UserDAO userDAO = new UserDAO();

    private int userId;
    private int otherUserId;

    @BeforeEach
    void setUp() throws SQLException {
        userId = insertTestUser("geardao_user");
        otherUserId = insertTestUser("geardao_other");
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Gear WHERE user_id IN (" + userId + ", " + otherUserId + ")");
            stmt.executeUpdate("DELETE FROM User WHERE id IN (" + userId + ", " + otherUserId + ")");
        }
    }

    @Test
    void addGearCreatesUncheckedItem() throws SQLException {
        gearDAO.addGear(userId, "Hiking Boots");

        List<Gear> gearList = gearDAO.getByUser(userId);
        assertEquals(1, gearList.size());
        assertEquals("Hiking Boots", gearList.get(0).getName());
        assertFalse(gearList.get(0).isChecked());
    }

    @Test
    void getByUserReturnsOnlyThatUsersGearOrderedById() throws SQLException {
        gearDAO.addGear(userId, "Tent");
        gearDAO.addGear(userId, "Sleeping Bag");
        gearDAO.addGear(otherUserId, "Backpack"); // different user, should not show up

        List<Gear> gearList = gearDAO.getByUser(userId);
        assertEquals(2, gearList.size());
        assertEquals("Tent", gearList.get(0).getName());
        assertEquals("Sleeping Bag", gearList.get(1).getName());
    }

    @Test
    void getCountByUserMatchesActualNumberOfItems() throws SQLException {
        assertEquals(0, gearDAO.getCountByUser(userId));

        gearDAO.addGear(userId, "Compass");
        gearDAO.addGear(userId, "Map");
        gearDAO.addGear(otherUserId, "Rope"); // different user, should not count

        assertEquals(2, gearDAO.getCountByUser(userId));
    }

    @Test
    void toggleCheckFlipsCurrentState() throws SQLException {
        gearDAO.addGear(userId, "Water Bottle");
        int gearId = gearDAO.getByUser(userId).get(0).getId();

        gearDAO.toggleCheck(gearId, false); // was unchecked, should become checked
        Gear afterFirstToggle = gearDAO.getByUser(userId).get(0);
        assertTrue(afterFirstToggle.isChecked());

        gearDAO.toggleCheck(gearId, true); // was checked, should become unchecked
        Gear afterSecondToggle = gearDAO.getByUser(userId).get(0);
        assertFalse(afterSecondToggle.isChecked());
    }

    @Test
    void deleteGearRemovesItemWhenOwnedByUser() throws SQLException {
        gearDAO.addGear(userId, "First Aid Kit");
        int gearId = gearDAO.getByUser(userId).get(0).getId();

        boolean deleted = gearDAO.deleteGear(gearId, userId);

        assertTrue(deleted);
        assertEquals(0, gearDAO.getByUser(userId).size());
    }

    @Test
    void deleteGearReturnsFalseAndDoesNothingWhenNotOwnedByUser() throws SQLException {
        gearDAO.addGear(userId, "Headlamp");
        int gearId = gearDAO.getByUser(userId).get(0).getId();

        // otherUserId tries to delete userId's gear — should be rejected
        boolean deleted = gearDAO.deleteGear(gearId, otherUserId);

        assertFalse(deleted);
        assertEquals(1, gearDAO.getByUser(userId).size()); // item still exists
    }

    // ---------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------

    private int insertTestUser(String usernamePrefix) throws SQLException {
        User user = new User(usernamePrefix + "_" + System.nanoTime(), "hash", "salt");
        userDAO.insert(user);
        return user.getId();
    }
}
