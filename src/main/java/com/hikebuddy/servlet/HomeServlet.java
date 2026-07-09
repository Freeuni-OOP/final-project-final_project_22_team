package com.hikebuddy.servlet;

import jakarta.servlet.annotation.WebServlet;
import com.hikebuddy.dao.HikeRouteDAO;
import com.hikebuddy.dao.JourneyDAO;
import com.hikebuddy.dao.NotificationDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.dao.UserDAO;
import com.hikebuddy.model.HikeRoute;
import com.hikebuddy.model.JourneyEntry;
import com.hikebuddy.model.StoryFolder;
import com.hikebuddy.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * URL: /home
 * Aggregates data from every other epic to build the dashboard.
 * Every DAO call is wrapped individually so one failure doesn't
 * crash the whole page (see Epic 9 pitfall notes).
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

	private final UserDAO userDAO = new UserDAO();
	private final HikeRouteDAO hikeRouteDAO = new HikeRouteDAO();
	private final JourneyDAO journeyDAO = new JourneyDAO();
	private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();
	private final NotificationDAO notificationDAO = new NotificationDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;

		if (sessionUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		int userId = sessionUser.getId();

		User user;
		try {
			User freshUser = userDAO.findByUsername(sessionUser.getUsername());
			user = (freshUser != null) ? freshUser : sessionUser;
		} catch (SQLException e) {
			user = sessionUser;
		}
		request.setAttribute("user", user);

		List<HikeRoute> suggested;
		try {
			List<HikeRoute> all = hikeRouteDAO.getSuggested(userId, user.getHikingLevel());
			suggested = all.size() > 3 ? all.subList(0, 3) : all;
		} catch (SQLException e) {
			suggested = Collections.emptyList();
		}
		request.setAttribute("suggestedHikes", suggested);

		List<JourneyEntry> recentJourneys;
		try {
			recentJourneys = journeyDAO.getRecentCompleted(userId, 3);
		} catch (SQLException e) {
			recentJourneys = Collections.emptyList();
		}
		request.setAttribute("recentJourneys", recentJourneys);

		List<StoryFolder> folders;
		try {
			List<StoryFolder> all = storyFolderDAO.getFoldersByUser(userId);
			folders = all.size() > 2 ? all.subList(0, 2) : all;
		} catch (SQLException e) {
			folders = Collections.emptyList();
		}
		request.setAttribute("recentFolders", folders);

		int unreadCount;
		try {
			unreadCount = notificationDAO.getUnreadCount(userId);
		} catch (SQLException e) {
			unreadCount = 0;
		}
		request.setAttribute("unreadCount", unreadCount);

		request.getRequestDispatcher("/jsp/home.jsp").forward(request, response);
	}
}