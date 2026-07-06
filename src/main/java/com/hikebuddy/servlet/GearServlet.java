package com.hikebuddy.servlet;

import com.hikebuddy.dao.GearDAO;
import com.hikebuddy.model.Gear;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/gear")
public class GearServlet extends HttpServlet {

    private final GearDAO gearDAO = new GearDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get session and validate
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        int userId = sessionUser.getId();
        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                if (name != null && !name.trim().isEmpty()) {
                    gearDAO.addGear(userId, name.trim());
                }

            } else if ("toggle".equals(action)) {
                int gearId = Integer.parseInt(request.getParameter("gearId"));

                // Verify ownership — only toggle if gear belongs to this user
                List<Gear> userGear = gearDAO.getByUser(userId);
                boolean ownsGear = userGear.stream().anyMatch(g -> g.getId() == gearId);

                if (ownsGear) {
                    // Read current state from DB instead of trusting form field
                    boolean currentState = userGear.stream()
                            .filter(g -> g.getId() == gearId)
                            .findFirst()
                            .map(Gear::isChecked)
                            .orElse(false);
                    gearDAO.toggleCheck(gearId, currentState);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }
}
