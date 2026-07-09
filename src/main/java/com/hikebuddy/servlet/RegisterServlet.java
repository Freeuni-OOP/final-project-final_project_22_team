package com.hikebuddy.servlet;

import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.User;
import com.hikebuddy.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    // GET — just show the registration form
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }

    // POST — process the registration form
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read form inputs
        String username        = request.getParameter("username");
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 2. Validate — check for empty fields
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {

            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        // 3. Validate — check passwords match
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        // 4. Validate — check username is not already taken
        try {
            User existing = userDAO.findByUsername(username.trim());
            if (existing != null) {
                request.setAttribute("error", "Username is already taken. Please choose another.");
                request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
                return;
            }

            // 5. All good — hash password and create user
            String salt         = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            User newUser = new User(username.trim(), passwordHash, salt);
            userDAO.insert(newUser);

            // 6. Redirect to login with success message
            response.sendRedirect(request.getContextPath() + "/login?success=registered");

        } catch (SQLException e) {
            // Database error — show generic error to user, print details for developer
            e.printStackTrace();
            request.setAttribute("error", "Something went wrong. Please try again.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }
}