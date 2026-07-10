package com.hikebuddy.servlet;

import com.hikebuddy.dao.BadgeDAO;
import com.hikebuddy.dao.GearDAO;
import com.hikebuddy.model.Badge;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/gear")
public class GearServlet extends HttpServlet {

    private final GearDAO gearDAO = new GearDAO();
    private final BadgeDAO badgeDAO = new BadgeDAO();

    private static final int GEAR_COLLECTOR_THRESHOLD = 5;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get logged-in user from session
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("user");
        int userId = sessionUser.getId();

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                if (name == null || name.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/profile?error=emptygear");
                    return;
                }
                gearDAO.addGear(userId, name.trim());

                if (gearDAO.getCountByUser(userId) >= GEAR_COLLECTOR_THRESHOLD) {
                    badgeDAO.awardIfNotExists(userId, Badge.GEAR_COLLECTOR);
                }

            } else if ("toggle".equals(action)) {
                // Read gear id and current state
                int gearId = Integer.parseInt(request.getParameter("gearId"));
                boolean currentState = Boolean.parseBoolean(request.getParameter("currentState"));
                gearDAO.toggleCheck(gearId, currentState);

            } else if ("delete".equals(action)) {
                int gearId = Integer.parseInt(request.getParameter("gearId"));
                gearDAO.deleteGear(gearId, userId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Always redirect back to profile
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}