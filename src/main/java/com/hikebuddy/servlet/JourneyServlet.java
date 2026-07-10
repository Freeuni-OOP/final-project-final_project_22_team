package com.hikebuddy.servlet;

import com.hikebuddy.dao.BadgeDAO;
import com.hikebuddy.dao.HikeRouteDAO;
import com.hikebuddy.dao.JourneyDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.model.Badge;
import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.hikebuddy.util.FriendRequestBadgeHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/journey")
public class JourneyServlet extends HttpServlet {

    private final JourneyDAO journeyDAO = new JourneyDAO();
    private final HikeRouteDAO hikeRouteDAO = new HikeRouteDAO();
    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();
    private final BadgeDAO badgeDAO = new BadgeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        FriendRequestBadgeHelper.loadUnseenCount(request, user.getId());

        List<JourneyEntry> plannedEntries;
        try {
            plannedEntries = journeyDAO.getByUserAndStatuses(user.getId(), List.of("PENDING", "COMPLETED"));
        } catch (SQLException e) {
            e.printStackTrace();
            plannedEntries = new ArrayList<>();
        }
        request.setAttribute("plannedEntries", plannedEntries);

        List<JourneyEntry> wishlistEntries;
        try {
            wishlistEntries = journeyDAO.getWishlist(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            wishlistEntries = new ArrayList<>();
        }
        request.setAttribute("wishlistEntries", wishlistEntries);

        List<HikeRoute> hikeRoutes;
        try {
            hikeRoutes = hikeRouteDAO.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            hikeRoutes = new ArrayList<>();
        }
        request.setAttribute("hikeRoutes", hikeRoutes);

        request.getRequestDispatcher("/jsp/journey.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                handleAdd(request, response, user);
                return; // handleAdd does its own redirect/forward
            } else if ("status".equals(action)) {
                int entryId = Integer.parseInt(request.getParameter("entryId"));
                String newStatus = request.getParameter("newStatus");

                JourneyEntry entry = journeyDAO.getById(entryId, user.getId());
                if (entry == null) {
                    // Not found, or not owned by this user — nothing to do.
                    response.sendRedirect(request.getContextPath() + "/journey");
                    return;
                }

                boolean validTransition =
                        ("WISHLIST".equals(entry.getStatus()) && "PENDING".equals(newStatus))
                                || ("PENDING".equals(entry.getStatus()) && "COMPLETED".equals(newStatus));

                if (!validTransition) {
                    response.sendRedirect(request.getContextPath() + "/journey");
                    return;
                }

                journeyDAO.updateStatus(entryId, newStatus, user.getId());

                if ("COMPLETED".equals(newStatus)) {
                    int completedCount = journeyDAO.getCountByUser(user.getId());
                    if (completedCount >= 1) {
                        badgeDAO.awardIfNotExists(user.getId(), Badge.FIRST_HIKE);
                    }
                    if (completedCount >= 10) {
                        badgeDAO.awardIfNotExists(user.getId(), Badge.TEN_HIKES);
                    }
                }
            } else if ("delete".equals(action)) {
                int entryId = Integer.parseInt(request.getParameter("entryId"));
                journeyDAO.deleteEntry(entryId, user.getId());
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/journey");
    }

    /**
     * Handles action=add: validates the form, inserts the JourneyEntry (5.6),
     * then auto-creates a StoryFolder for it (5.7).
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException, SQLException {

        String hikeRouteIdParam = request.getParameter("hikeRouteId");
        String dateParam = request.getParameter("date");
        String distanceParam = request.getParameter("distance");
        String difficulty = request.getParameter("difficulty");
        String notes = request.getParameter("notes");
        String status = request.getParameter("status");

        if (hikeRouteIdParam == null || hikeRouteIdParam.isEmpty()
                || dateParam == null || dateParam.isEmpty()
                || distanceParam == null || distanceParam.isEmpty()
                || difficulty == null || difficulty.isEmpty()
                || status == null || status.isEmpty()) {
            forwardWithError(request, response, "All fields except notes are required.");
            return;
        }

        int hikeRouteId;
        double distance;
        java.sql.Date date;
        try {
            hikeRouteId = Integer.parseInt(hikeRouteIdParam);
            distance = Double.parseDouble(distanceParam);
            date = java.sql.Date.valueOf(LocalDate.parse(dateParam));
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            forwardWithError(request, response, "Invalid date or distance value.");
            return;
        }

        if (!"PENDING".equals(status) && !"WISHLIST".equals(status)) {
            forwardWithError(request, response, "Invalid status.");
            return;
        }

        JourneyEntry entry = new JourneyEntry();
        entry.setUserId(user.getId());
        entry.setHikeRouteId(hikeRouteId);
        entry.setDate(date);
        entry.setDistance(distance);
        entry.setDifficulty(difficulty);
        entry.setStatus(status);
        entry.setNotes(notes);

        int entryId = journeyDAO.addEntry(entry);

        // 5.7: auto-create a StoryFolder for this new entry.
        String routeName = "Hike";
        try {
            HikeRoute route = hikeRouteDAO.getById(hikeRouteId);
            if (route != null) {
                routeName = route.getName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Non-fatal — folder still gets created with the fallback name below.
        }
        String folderName = routeName + " — " + dateParam;
        try {
            storyFolderDAO.createFolder(user.getId(), entryId, folderName);
        } catch (SQLException e) {
            // Folder creation failure should not roll back the journey entry itself.
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/journey");
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("formError", message);
        try {
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute("user");
            request.setAttribute("plannedEntries", journeyDAO.getByUserAndStatuses(user.getId(), List.of("PENDING", "COMPLETED")));
            request.setAttribute("wishlistEntries", journeyDAO.getWishlist(user.getId()));
            request.setAttribute("hikeRoutes", hikeRouteDAO.getAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/jsp/journey.jsp").forward(request, response);
    }
}