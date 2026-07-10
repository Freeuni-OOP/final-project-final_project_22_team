package com.hikebuddy.dao;

import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class HikeRouteDAOTest {

    private HikeRouteDAO hikeRouteDAO;
    private UserDAO userDAO;
    private int testRouteId = -1;
    private int testUserId = -1;

    @Before
    public void setUp() throws SQLException {
        hikeRouteDAO = new HikeRouteDAO();
        userDAO = new UserDAO();

        // Insert a test route directly via SQL
        String sql = "INSERT INTO HikeRoute (name, region, difficulty, distance, description) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Test Trail Unit");
            stmt.setString(2, "Test Region");
            stmt.setString(3, "EASY");
            stmt.setDouble(4, 5.0);
            stmt.setString(5, "A test trail");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) testRouteId = keys.getInt(1);
            }
        }

        // Insert a test user for getSuggested
        User user = new User("testuser_hikeroute", "hash", "salt");
        user.setHikingLevel("BEGINNER");
        userDAO.insert(user);
        testUserId = user.getId();
    }

    @After
    public void tearDown() throws SQLException {
        // Delete test route
        if (testRouteId > 0) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM HikeRoute WHERE id = ?")) {
                stmt.setInt(1, testRouteId);
                stmt.executeUpdate();
            }
        }
        // Delete test user
        if (testUserId > 0) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM User WHERE id = ?")) {
                stmt.setInt(1, testUserId);
                stmt.executeUpdate();
            }
        }
    }

    // --- getAll() ---

    @Test
    public void testGetAllReturnsNonEmptyList() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        assertNotNull(routes);
        assertFalse("getAll should return at least the test route", routes.isEmpty());
    }

    @Test
    public void testGetAllReturnsCorrectFields() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        boolean found = routes.stream()
                .anyMatch(r -> r.getName().equals("Test Trail Unit"));
        assertTrue("Test route should appear in getAll", found);
    }

    @Test
    public void testGetAllOrderedByName() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        for (int i = 0; i < routes.size() - 1; i++) {
            assertTrue(
                    routes.get(i).getName().compareToIgnoreCase(
                            routes.get(i + 1).getName()) <= 0
            );
        }
    }

    // --- getById() ---

    @Test
    public void testGetByIdReturnsCorrectRoute() throws SQLException {
        HikeRoute route = hikeRouteDAO.getById(testRouteId);
        assertNotNull(route);
        assertEquals("Test Trail Unit", route.getName());
        assertEquals("Test Region", route.getRegion());
        assertEquals("EASY", route.getDifficulty());
        assertEquals(5.0, route.getDistance(), 0.001);
    }

    @Test
    public void testGetByIdReturnsNullForMissingId() throws SQLException {
        HikeRoute route = hikeRouteDAO.getById(-9999);
        assertNull(route);
    }

    // --- getByDifficulty() ---

    @Test
    public void testGetByDifficultyReturnsOnlyMatchingRoutes() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("EASY");
        assertNotNull(routes);
        for (HikeRoute route : routes) {
            assertEquals("EASY", route.getDifficulty());
        }
    }

    @Test
    public void testGetByDifficultyIncludesTestRoute() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("EASY");
        boolean found = routes.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue("Test EASY route should appear in getByDifficulty", found);
    }

    @Test
    public void testGetByDifficultyReturnsEmptyForNoMatch() throws SQLException {
        // Insert a HARD-only route — if seed data has no HARD, this catches that
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("HARD");
        assertNotNull(routes);
        // Every result must be HARD
        for (HikeRoute r : routes) {
            assertEquals("HARD", r.getDifficulty());
        }
    }

    // --- searchByNameOrRegion() ---

    @Test
    public void testSearchByNameFindsTestRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Trail Unit");
        assertFalse(results.isEmpty());
        boolean found = results.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue(found);
    }

    @Test
    public void testSearchByRegionFindsTestRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Region");
        assertFalse(results.isEmpty());
        boolean found = results.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue(found);
    }

    @Test
    public void testSearchByPartialNameFindsRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Trail");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testSearchReturnsEmptyForNoMatch() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion(
                "xyznonexistentroute999");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // --- getSuggested() ---

    @Test
    public void testGetSuggestedReturnsMaxSixRoutes() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "BEGINNER");
        assertNotNull(suggested);
        assertTrue("getSuggested should return at most 6 routes",
                suggested.size() <= 6);
    }

    @Test
    public void testGetSuggestedReturnsEasyForBeginner() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "BEGINNER");
        for (HikeRoute route : suggested) {
            assertEquals("BEGINNER users should get EASY routes",
                    "EASY", route.getDifficulty());
        }
    }

    @Test
    public void testGetSuggestedReturnsMediumForIntermediate() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "INTERMEDIATE");
        for (HikeRoute route : suggested) {
            assertEquals("INTERMEDIATE users should get MEDIUM routes",
                    "MEDIUM", route.getDifficulty());
        }
    }

    @Test
    public void testGetSuggestedReturnsHardForAdvanced() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "ADVANCED");
        for (HikeRoute route : suggested) {
            assertEquals("ADVANCED users should get HARD routes",
                    "HARD", route.getDifficulty());
        }
    }

    @Test
    public void testGetSuggestedReturnsListForUnknownLevel() throws SQLException {
        // Unknown level should default to EASY
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "UNKNOWN");
        assertNotNull(suggested);
        for (HikeRoute route : suggested) {
            assertEquals("Unknown level should default to EASY routes",
                    "EASY", route.getDifficulty());
        }
    }
}