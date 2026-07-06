package com.hikebuddy.servlet;

import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

            // 4. Forward to profile.jsp
            request.getRequestDispatcher("/jsp/profile.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/home");
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