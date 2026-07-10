package com.hikebuddy.servlet;

import com.hikebuddy.dao.FriendDAO;
import com.hikebuddy.dao.HikeRouteDAO;
import com.hikebuddy.dao.JourneyDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.model.StoryFolder;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/explore")
public class ExploreServlet extends HttpServlet {

    private final HikeRouteDAO hikeRouteDAO = new HikeRouteDAO();
    private final JourneyDAO journeyDAO = new JourneyDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User loggedInUser = (User) session.getAttribute("user");
        int userId = loggedInUser.getId();
        String hikingLevel = loggedInUser.getHikingLevel();

        // Search and filter
        String query = request.getParameter("q");
        String diff = request.getParameter("diff");

        if ((query != null && !query.trim().isEmpty())
                || (diff != null && !diff.trim().isEmpty() && !"ALL".equals(diff))) {
            try {
                List<HikeRoute> searchResults;
                if (query != null && !query.trim().isEmpty()) {
                    searchResults = hikeRouteDAO.searchByNameOrRegion(query.trim());
                } else {
                    searchResults = hikeRouteDAO.getByDifficulty(diff.trim());
                }
                request.setAttribute("searchResults", searchResults);
                request.setAttribute("searchQuery", query);
                request.setAttribute("searchDiff", diff);
            } catch (SQLException e) {
                request.setAttribute("searchResults", new ArrayList<>());
            }
        }

        // Suggested hikes based on hiking level
        try {
            List<HikeRoute> suggested = hikeRouteDAO.getSuggested(userId, hikingLevel);
            request.setAttribute("suggestedHikes", suggested);
        } catch (SQLException e) {
            request.setAttribute("suggestedHikes", new ArrayList<>());
        }

        // User's own recent completed hikes
        try {
            List<JourneyEntry> recentHikes = journeyDAO.getRecentCompleted(userId, 3);
            request.setAttribute("recentHikes", recentHikes);
        } catch (SQLException e) {
            request.setAttribute("recentHikes", new ArrayList<>());
        }

        // Friends' recent hikes
        try {
            List<User> friends = friendDAO.getFriends(userId);
            List<Integer> friendIds = new ArrayList<>();
            for (User friend : friends) {
                friendIds.add(friend.getId());
            }
            List<JourneyEntry> friendHikes = new ArrayList<>();
            if (!friendIds.isEmpty()) {
                friendHikes = journeyDAO.getRecentCompletedForUsers(friendIds, 2);
            }
            request.setAttribute("friendHikes", friendHikes);
        } catch (SQLException e) {
            request.setAttribute("friendHikes", new ArrayList<>());
        }

        // Recent storyboard folders
        try {
            List<StoryFolder> allFolders = storyFolderDAO.getFoldersByUser(userId);
            List<StoryFolder> recentFolders = allFolders.size() > 2 ? allFolders.subList(0, 2) : allFolders;
            request.setAttribute("recentFolders", recentFolders);
        } catch (SQLException e) {
            request.setAttribute("recentFolders", new ArrayList<>());
        }

        request.getRequestDispatcher("/jsp/explore.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User loggedInUser = (User) session.getAttribute("user");
        int userId = loggedInUser.getId();
        String action = request.getParameter("action");

        try {
            // FIX 2 — validate hikeRouteId exists and is a valid number
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

            // FIX 2 — verify route actually exists in DB
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
                entry.setDifficulty(route.getDifficulty()); // use DB value, not form value
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

        response.sendRedirect(request.getContextPath() + "/explore");
    }
}