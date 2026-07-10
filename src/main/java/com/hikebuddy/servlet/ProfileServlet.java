package com.hikebuddy.servlet;

import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.hikebuddy.dao.GearDAO;
import com.hikebuddy.model.Gear;
import java.util.List;

import com.hikebuddy.dao.CalendarDAO;
import java.util.Set;

import com.hikebuddy.dao.BadgeDAO;
import com.hikebuddy.model.Badge;

import com.hikebuddy.dao.JourneyDAO;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get logged-in user from session
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("user");

        try {
            // 2. Load fresh user data from DB using session user's id
            // (session data may be stale if profile was updated)
            User user = userDAO.findById(sessionUser.getId());

            // 3. Set user as request attribute for the JSP
            request.setAttribute("user", user);

            // Load gear list
            GearDAO gearDAO = new GearDAO();
            List<Gear> gearList = gearDAO.getByUser(user.getId());
            request.setAttribute("gearList", gearList);

            // Load count of completed hikes
            JourneyDAO journeyDAO = new JourneyDAO();
            int hikeCount = journeyDAO.getCountByUser(user.getId());
            request.setAttribute("hikeCount", hikeCount);

            // Load available days for current month
            CalendarDAO calendarDAO = new CalendarDAO();
            java.time.LocalDate now = java.time.LocalDate.now();
            Set<Integer> availableDays = calendarDAO.getAvailableDays(user.getId(), now.getYear(), now.getMonthValue());
            request.setAttribute("availableDays", availableDays);

            // Load badges
            BadgeDAO badgeDAO = new BadgeDAO();
            List<Badge> badges = badgeDAO.getByUser(user.getId());
            request.setAttribute("badges", badges);

            // Load friend count and friends preview
            com.hikebuddy.dao.FriendDAO friendDAO = new com.hikebuddy.dao.FriendDAO();
            try {
                int friendCount = friendDAO.getFriendCount(user.getId());
                request.setAttribute("friendCount", friendCount);

                List<User> friendsPreview = friendDAO.getFriends(user.getId());
                if (friendsPreview.size() > 5) {
                    friendsPreview = friendsPreview.subList(0, 5);
                }
                request.setAttribute("friendsPreview", friendsPreview);
            } catch (SQLException e) {
                request.setAttribute("friendCount", 0);
                request.setAttribute("friendsPreview", new java.util.ArrayList<>());
            }

            // 5. Forward to profile.jsp
            request.getRequestDispatcher("/jsp/profile.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/explore");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get logged-in user from session
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("user");

        // 2. Read form inputs
        String bio         = request.getParameter("bio");
        String hikingLevel = request.getParameter("hikingLevel");

        // 3. Validate
        if (hikingLevel == null || hikingLevel.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        try {
            // 4. Build updated user object
            User updatedUser = userDAO.findById(sessionUser.getId());
            updatedUser.setBio(bio != null ? bio.trim() : "");
            updatedUser.setHikingLevel(hikingLevel.trim());

            // 5. Save to DB
            userDAO.updateProfile(updatedUser);

            // 6. Update session with fresh data
            session.setAttribute("user", updatedUser);

            // 7. Redirect back to profile (PRG pattern)
            response.sendRedirect(request.getContextPath() + "/profile?success=updated");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}