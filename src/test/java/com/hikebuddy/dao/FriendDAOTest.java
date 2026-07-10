package com.hikebuddy.dao;

import com.hikebuddy.model.FriendRequest;
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
 * Integration tests for FriendDAO. Runs against the real MySQL database
 * configured in db.properties — FriendDAO has no mocking seam, it calls
 * DBHelper.getConnection() directly. Each test builds its own User fixtures
 * and deletes them (and related FriendRequest/Friendship rows) in tearDown
 * so runs don't leak rows into the shared dev database.
 */
class FriendDAOTest {

    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();

    private int userAId;
    private int userBId;

    @BeforeEach
    void setUp() throws SQLException {
        userAId = insertTestUser("frienddao_a");
        userBId = insertTestUser("frienddao_b");
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM FriendRequest WHERE sender_id IN (" + userAId + ", " + userBId + ") " +
                    "OR receiver_id IN (" + userAId + ", " + userBId + ")");
            stmt.executeUpdate("DELETE FROM Friendship WHERE user_id_1 IN (" + userAId + ", " + userBId + ") " +
                    "OR user_id_2 IN (" + userAId + ", " + userBId + ")");
            stmt.executeUpdate("DELETE FROM User WHERE id IN (" + userAId + ", " + userBId + ")");
        }
    }

    @Test
    void sendRequestCreatesPendingRequest() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);

        List<FriendRequest> incoming = friendDAO.getIncomingRequests(userBId);
        assertEquals(1, incoming.size());
        assertEquals("PENDING", incoming.get(0).getStatus());
        assertEquals(userAId, incoming.get(0).getSenderId());
    }

    @Test
    void sendRequestAfterDeclineResetsToPending() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();
        friendDAO.declineRequest(requestId);

        friendDAO.sendRequest(userAId, userBId);

        List<FriendRequest> incoming = friendDAO.getIncomingRequests(userBId);
        assertEquals(1, incoming.size());
        assertEquals("PENDING", incoming.get(0).getStatus());
    }

    @Test
    void acceptRequestCreatesFriendshipAndMarksAccepted() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();

        friendDAO.acceptRequest(requestId, userAId, userBId);

        assertTrue(friendDAO.areFriends(userAId, userBId));

        List<FriendRequest> stillPending = friendDAO.getIncomingRequests(userBId);
        assertTrue(stillPending.isEmpty());
    }

    @Test
    void acceptRequestCalledTwiceIsNoOpOnSecondCall() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();

        friendDAO.acceptRequest(requestId, userAId, userBId);
        assertDoesNotThrow(() -> friendDAO.acceptRequest(requestId, userAId, userBId));

        assertEquals(1, friendDAO.getFriendCount(userAId));
    }

    @Test
    void declineRequestSetsStatusToDeclined() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();

        friendDAO.declineRequest(requestId);

        List<FriendRequest> incoming = friendDAO.getIncomingRequests(userBId);
        assertTrue(incoming.isEmpty());
    }

    @Test
    void getFriendsReturnsBothDirectionsCorrectly() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();
        friendDAO.acceptRequest(requestId, userAId, userBId);

        List<User> aFriends = friendDAO.getFriends(userAId);
        List<User> bFriends = friendDAO.getFriends(userBId);

        assertEquals(1, aFriends.size());
        assertEquals(userBId, aFriends.get(0).getId());

        assertEquals(1, bFriends.size());
        assertEquals(userAId, bFriends.get(0).getId());
    }

    @Test
    void removeFriendDeletesFriendshipBothWays() throws SQLException {
        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();
        friendDAO.acceptRequest(requestId, userAId, userBId);

        friendDAO.removeFriend(userAId, userBId);

        assertFalse(friendDAO.areFriends(userAId, userBId));
        assertEquals(0, friendDAO.getFriendCount(userAId));
        assertEquals(0, friendDAO.getFriendCount(userBId));
    }

    @Test
    void getFriendCountReflectsNumberOfFriendships() throws SQLException {
        assertEquals(0, friendDAO.getFriendCount(userAId));

        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();
        friendDAO.acceptRequest(requestId, userAId, userBId);

        assertEquals(1, friendDAO.getFriendCount(userAId));
    }

    @Test
    void areFriendsReturnsFalseBeforeAcceptingAndTrueAfter() throws SQLException {
        assertFalse(friendDAO.areFriends(userAId, userBId));

        friendDAO.sendRequest(userAId, userBId);
        int requestId = friendDAO.getIncomingRequests(userBId).get(0).getId();
        friendDAO.acceptRequest(requestId, userAId, userBId);

        assertTrue(friendDAO.areFriends(userAId, userBId));
    }

    @Test
    void hasPendingRequestChecksBothDirections() throws SQLException {
        assertFalse(friendDAO.hasPendingRequest(userAId, userBId));

        friendDAO.sendRequest(userAId, userBId);

        assertTrue(friendDAO.hasPendingRequest(userAId, userBId));
        assertTrue(friendDAO.hasPendingRequest(userBId, userAId));
    }

    private int insertTestUser(String usernamePrefix) throws SQLException {
        User user = new User(usernamePrefix + "_" + System.nanoTime(), "hash", "salt");
        userDAO.insert(user);
        return user.getId();
    }
}
