package com.hikebuddy.servlet;

import com.hikebuddy.dao.CalendarDAO;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {

    private final CalendarDAO calendarDAO = new CalendarDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get logged-in user from session
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("user");
        int userId = sessionUser.getId();

        // Read the date parameter (format: yyyy-MM-dd)
        String dateParam = request.getParameter("date");

        try {
            LocalDate date = LocalDate.parse(dateParam);
            calendarDAO.toggleDay(userId, date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect back to profile
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}
