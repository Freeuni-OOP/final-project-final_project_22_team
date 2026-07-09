package com.hikebuddy.servlet;

import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.User;
import com.hikebuddy.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    // GET — just show the login form
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
    }

    // POST — process the login form
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read form inputs
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 2. Validate — check for empty fields
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
            return;
        }

        try {
            // 3. Look up user by username
            User user = userDAO.findByUsername(username.trim());

            // 4. If user not found OR password wrong — same generic error
            // (never reveal which one was wrong — security best practice)
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
                request.setAttribute("error", "Invalid username or password.");
                request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
                return;
            }

            // 5. Success — create session and store user
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

            // 6. Redirect to home page
            response.sendRedirect(request.getContextPath() + "/home");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Something went wrong. Please try again.");
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
        }
    }
}