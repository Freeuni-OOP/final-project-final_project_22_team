package com.hikebuddy.dao;

import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HikeRouteDAOTest {

    private HikeRouteDAO hikeRouteDAO;
    private UserDAO userDAO;
    private int testRouteId = -1;
    private int testUserId = -1;

    @BeforeEach
    void setUp() throws SQLException {
        hikeRouteDAO = new HikeRouteDAO();
        userDAO = new UserDAO();

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

        User user = new User("testuser_hikeroute_dao", "hash", "salt");
        user.setHikingLevel("BEGINNER");
        userDAO.insert(user);
        testUserId = user.getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testRouteId > 0) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM HikeRoute WHERE id = ?")) {
                stmt.setInt(1, testRouteId);
                stmt.executeUpdate();
            }
        }
        if (testUserId > 0) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM User WHERE id = ?")) {
                stmt.setInt(1, testUserId);
                stmt.executeUpdate();
            }
        }
    }

    @Test
    void testGetAllReturnsNonEmptyList() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        assertNotNull(routes);
        assertFalse(routes.isEmpty());
    }

    @Test
    void testGetAllContainsTestRoute() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        boolean found = routes.stream()
                .anyMatch(r -> r.getName().equals("Test Trail Unit"));
        assertTrue(found);
    }

    @Test
    void testGetAllOrderedByName() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getAll();
        for (int i = 0; i < routes.size() - 1; i++) {
            assertTrue(
                    routes.get(i).getName().compareToIgnoreCase(
                            routes.get(i + 1).getName()) <= 0
            );
        }
    }

    @Test
    void testGetByIdReturnsCorrectRoute() throws SQLException {
        HikeRoute route = hikeRouteDAO.getById(testRouteId);
        assertNotNull(route);
        assertEquals("Test Trail Unit", route.getName());
        assertEquals("Test Region", route.getRegion());
        assertEquals("EASY", route.getDifficulty());
        assertEquals(5.0, route.getDistance(), 0.001);
    }

    @Test
    void testGetByIdReturnsNullForMissingId() throws SQLException {
        HikeRoute route = hikeRouteDAO.getById(-9999);
        assertNull(route);
    }

    @Test
    void testGetByDifficultyReturnsOnlyMatchingRoutes() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("EASY");
        assertNotNull(routes);
        for (HikeRoute route : routes) {
            assertEquals("EASY", route.getDifficulty());
        }
    }

    @Test
    void testGetByDifficultyIncludesTestRoute() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("EASY");
        boolean found = routes.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue(found);
    }

    @Test
    void testGetByDifficultyHardRoutesAreAllHard() throws SQLException {
        List<HikeRoute> routes = hikeRouteDAO.getByDifficulty("HARD");
        assertNotNull(routes);
        for (HikeRoute r : routes) {
            assertEquals("HARD", r.getDifficulty());
        }
    }

    @Test
    void testSearchByNameFindsTestRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Trail Unit");
        assertFalse(results.isEmpty());
        boolean found = results.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue(found);
    }

    @Test
    void testSearchByRegionFindsTestRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Region");
        assertFalse(results.isEmpty());
        boolean found = results.stream()
                .anyMatch(r -> r.getId() == testRouteId);
        assertTrue(found);
    }

    @Test
    void testSearchByPartialNameFindsRoute() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion("Test Trail");
        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchReturnsEmptyForNoMatch() throws SQLException {
        List<HikeRoute> results = hikeRouteDAO.searchByNameOrRegion(
                "xyznonexistentroute999");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetSuggestedReturnsMaxSixRoutes() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "BEGINNER");
        assertNotNull(suggested);
        assertTrue(suggested.size() <= 6);
    }

    @Test
    void testGetSuggestedReturnsEasyForBeginner() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "BEGINNER");
        for (HikeRoute route : suggested) {
            assertEquals("EASY", route.getDifficulty());
        }
    }

    @Test
    void testGetSuggestedReturnsMediumForIntermediate() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "INTERMEDIATE");
        for (HikeRoute route : suggested) {
            assertEquals("MEDIUM", route.getDifficulty());
        }
    }

    @Test
    void testGetSuggestedReturnsHardForAdvanced() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "ADVANCED");
        for (HikeRoute route : suggested) {
            assertEquals("HARD", route.getDifficulty());
        }
    }

    @Test
    void testGetSuggestedUnknownLevelDefaultsToEasy() throws SQLException {
        List<HikeRoute> suggested = hikeRouteDAO.getSuggested(testUserId, "UNKNOWN");
        assertNotNull(suggested);
        for (HikeRoute route : suggested) {
            assertEquals("EASY", route.getDifficulty());
        }
    }
}