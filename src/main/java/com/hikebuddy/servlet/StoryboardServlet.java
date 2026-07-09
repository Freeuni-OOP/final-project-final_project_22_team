package com.hikebuddy.servlet;

import com.hikebuddy.dao.PhotoDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.model.Photo;
import com.hikebuddy.model.StoryFolder;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/storyboard")
public class StoryboardServlet extends HttpServlet {

    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();
    private final PhotoDAO photoDAO = new PhotoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            List<StoryFolder> folders = storyFolderDAO.getFoldersByUser(user.getId());
            request.setAttribute("folders", folders);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("folders", new java.util.ArrayList<>());
        }

        request.getRequestDispatcher("/jsp/storyboard.jsp").forward(request, response);
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
            if ("create".equals(action)) {
                String folderName = request.getParameter("folderName");
                if (folderName != null && !folderName.trim().isEmpty()) {
                    // journeyEntryId is null since this is a manually created folder
                    storyFolderDAO.createFolder(user.getId(), null, folderName.trim());
                }

            } else if ("delete".equals(action)) {
                int folderId = Integer.parseInt(request.getParameter("folderId"));

                // Verify ownership before deleting
                List<StoryFolder> userFolders = storyFolderDAO.getFoldersByUser(user.getId());
                boolean ownsFolder = userFolders.stream().anyMatch(f -> f.getId() == folderId);

                if (ownsFolder) {
                    // Delete all photos from disk first
                    List<Photo> photos = photoDAO.getPhotosByFolder(folderId);
                    for (Photo photo : photos) {
                        String realPath = getServletContext().getRealPath(photo.getFilePath());
                        if (realPath != null) {
                            new File(realPath).delete();
                        }
                    }

                    // Delete photo rows and folder row in a single transaction
                    storyFolderDAO.deleteFolderWithPhotos(folderId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/storyboard");
    }
}