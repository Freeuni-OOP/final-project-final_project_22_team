package com.hikebuddy.util;

import com.hikebuddy.dao.FriendDAO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Shared helper to load the unseen pending friend request count and set it
 * as a request attribute, so header.jsp can show the badge on every page.
 */
public class FriendRequestBadgeHelper {

    private static final FriendDAO friendDAO = new FriendDAO();

    public static void loadUnseenCount(HttpServletRequest request, int userId) {
        try {
            int count = friendDAO.getUnseenPendingCount(userId);
            request.setAttribute("unseenFriendRequests", count);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("unseenFriendRequests", 0);
        }
    }
}