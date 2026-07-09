package com.hikebuddy.servlet;

import com.hikebuddy.dao.HikeRouteDAO;
import com.hikebuddy.dao.JourneyDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet("/hike")
public class HikeDetailServlet extends HttpServlet {

    private final HikeRouteDAO hikeRouteDAO = new HikeRouteDAO();
    private final JourneyDAO journeyDAO = new JourneyDAO();
    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Read and validate id param
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int hikeRouteId;
        try {
            hikeRouteId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Load route from DB
        try {
            HikeRoute route = hikeRouteDAO.getById(hikeRouteId);
            if (route == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("route", route);
        } catch (SQLException e) {
            throw new ServletException("Database error loading hike detail", e);
        }

        request.getRequestDispatcher("/jsp/hikeDetail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User loggedInUser = (User) session.getAttribute("user");
        int userId = loggedInUser.getId();
        String action = request.getParameter("action");

        // Validate hikeRouteId
        String hikeRouteIdParam = request.getParameter("hikeRouteId");
        if (hikeRouteIdParam == null || hikeRouteIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/explore");
            return;
        }

        int hikeRouteId;
        try {
            hikeRouteId = Integer.parseInt(hikeRouteIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/explore");
            return;
        }

        try {
            // Verify route exists in DB
            HikeRoute route = hikeRouteDAO.getById(hikeRouteId);
            if (route == null) {
                response.sendRedirect(request.getContextPath() + "/explore");
                return;
            }

            if (!journeyDAO.existsForUser(userId, hikeRouteId)) {

                JourneyEntry entry = new JourneyEntry();
                entry.setUserId(userId);
                entry.setHikeRouteId(hikeRouteId);
                entry.setDate(java.sql.Date.valueOf(LocalDate.now()));
                entry.setDifficulty(route.getDifficulty());
                entry.setDistance(0);
                entry.setNotes("");

                if ("wishlist".equals(action)) {
                    entry.setStatus("WISHLIST");
                    journeyDAO.addEntry(entry);

                } else if ("plan".equals(action)) {
                    entry.setStatus("PENDING");
                    int entryId = journeyDAO.addEntry(entry);

                    String folderName = route.getName() + " — " + LocalDate.now();
                    try {
                        storyFolderDAO.createFolder(userId, entryId, folderName);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Redirect back to the same hike detail page
        response.sendRedirect(request.getContextPath() + "/hike?id=" + hikeRouteIdParam);
    }
}