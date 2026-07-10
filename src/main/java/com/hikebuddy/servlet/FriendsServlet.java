package com.hikebuddy.servlet;

import com.hikebuddy.dao.BadgeDAO;
import com.hikebuddy.dao.FriendDAO;
import com.hikebuddy.dao.NotificationDAO;
import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.Badge;
import com.hikebuddy.model.FriendRequest;
import com.hikebuddy.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/friends")
public class FriendsServlet extends HttpServlet {

    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final BadgeDAO badgeDAO = new BadgeDAO();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loggedInUser = (User) request.getSession().getAttribute("user");
        int userId = loggedInUser.getId();

        try {
            // Always load both lists — friends.jsp needs them regardless of search
            List<FriendRequest> incomingRequests = friendDAO.getIncomingRequests(userId);
            List<User> friends = friendDAO.getFriends(userId);

            request.setAttribute("incomingRequests", incomingRequests);
            request.setAttribute("friends", friends);

            // Only run search if action=search AND q param is present
            String action = request.getParameter("action");
            String query = request.getParameter("q");

            if ("search".equals(action) && query != null && !query.trim().isEmpty()) {
                List<User> searchResults = userDAO.searchByUsername(query.trim(), userId);
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchQuery", query.trim());
            }

        } catch (SQLException e) {
            throw new ServletException("Database error loading friends page", e);
        }

        request.getRequestDispatcher("/jsp/friends.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loggedInUser = (User) request.getSession().getAttribute("user");
        int userId = loggedInUser.getId();
        String action = request.getParameter("action");

        try {
            switch (action) {

                case "send": {
                    int targetUserId = Integer.parseInt(request.getParameter("targetUserId"));
                    // Guard: don't send if already friends or request already pending
                    if (!friendDAO.areFriends(userId, targetUserId)
                            && !friendDAO.hasPendingRequest(userId, targetUserId)) {
                        friendDAO.sendRequest(userId, targetUserId);
                        try {
                            notificationDAO.create(
                                    targetUserId,
                                    "FRIEND_REQUEST",
                                    loggedInUser.getUsername() + " sent you a friend request"
                            );
                        } catch (SQLException e) {
                            // notification failure must NOT break the friend request
                            e.printStackTrace();
                        }
                    }
                    break;
                }

                case "accept": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    int senderId = Integer.parseInt(request.getParameter("senderId"));
                    // userId is the receiver (logged-in user accepted the request)
                    friendDAO.acceptRequest(requestId, senderId, userId);
                    try {
                        notificationDAO.create(
                                senderId,
                                "FRIEND_ACCEPTED",
                                loggedInUser.getUsername() + " accepted your friend request"
                        );
                    } catch (SQLException e) {
                        // notification failure must NOT break the accept action
                        e.printStackTrace();
                    }

                    // Both sides just gained a friend — check each for the First Friend badge
                    if (friendDAO.getFriendCount(userId) >= 1) {
                        badgeDAO.awardIfNotExists(userId, Badge.FIRST_FRIEND);
                    }
                    if (friendDAO.getFriendCount(senderId) >= 1) {
                        badgeDAO.awardIfNotExists(senderId, Badge.FIRST_FRIEND);
                    }

                    break;
                }

                case "decline": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    friendDAO.declineRequest(requestId);
                    break;
                }

                case "remove": {
                    int friendId = Integer.parseInt(request.getParameter("friendId"));
                    friendDAO.removeFriend(userId, friendId);
                    break;
                }

                default:
                    // Unknown action — just redirect safely
                    break;
            }

        } catch (SQLException e) {
            throw new ServletException("Database error processing friend action", e);
        }

        // PRG pattern — always redirect after POST, never forward
        response.sendRedirect(request.getContextPath() + "/friends");
    }
}