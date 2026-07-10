package com.hikebuddy.servlet;

import com.hikebuddy.dao.PhotoDAO;
import com.hikebuddy.dao.StoryFolderDAO;
import com.hikebuddy.model.Photo;
import com.hikebuddy.model.StoryFolder;
import com.hikebuddy.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@WebServlet("/folder")
@MultipartConfig(maxFileSize = 10485760, maxRequestSize = 20971520)
public class FolderServlet extends HttpServlet {

    private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();
    private final PhotoDAO photoDAO = new PhotoDAO();

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        int folderId;
        try {
            folderId = Integer.parseInt(request.getParameter("folderId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/storyboard");
            return;
        }

        try {
            List<StoryFolder> userFolders = storyFolderDAO.getFoldersByUser(user.getId());
            StoryFolder folder = userFolders.stream()
                    .filter(f -> f.getId() == folderId)
                    .findFirst()
                    .orElse(null);

            if (folder == null) {
                response.sendRedirect(request.getContextPath() + "/storyboard");
                return;
            }

            List<Photo> photos = photoDAO.getPhotosByFolder(folderId);
            request.setAttribute("folder", folder);
            request.setAttribute("photos", photos);
            request.getRequestDispatcher("/jsp/folder.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/storyboard");
        }
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

        int folderId;
        try {
            folderId = Integer.parseInt(request.getParameter("folderId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/storyboard");
            return;
        }

        String action = request.getParameter("action");

        try {
            // Verify folder ownership before any action
            List<StoryFolder> userFolders = storyFolderDAO.getFoldersByUser(user.getId());
            boolean ownsFolder = userFolders.stream().anyMatch(f -> f.getId() == folderId);

            if (!ownsFolder) {
                response.sendRedirect(request.getContextPath() + "/storyboard");
                return;
            }

            if ("upload".equals(action)) {
                Part filePart = request.getPart("photo");
                String submittedFileName = filePart.getSubmittedFileName();

                if (submittedFileName != null && !submittedFileName.isEmpty()) {
                    String extension = "";
                    int dotIndex = submittedFileName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        extension = submittedFileName.substring(dotIndex).toLowerCase();
                    }

                    // Reject upload if extension is not in the whitelist
                    if (!ALLOWED_EXTENSIONS.contains(extension)) {
                        response.sendRedirect(request.getContextPath() + "/folder?folderId=" + folderId);
                        return;
                    }

                    String newFileName = UUID.randomUUID() + extension;
                    String uploadDir = getServletContext().getRealPath("/uploads/");

                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    filePart.write(uploadDir + File.separator + newFileName);
                    photoDAO.addPhoto(folderId, "/uploads/" + newFileName);
                }

            } else if ("deletePhoto".equals(action)) {
                int photoId;
                try {
                    photoId = Integer.parseInt(request.getParameter("photoId"));
                } catch (NumberFormatException e) {
                    response.sendRedirect(request.getContextPath() + "/folder?folderId=" + folderId);
                    return;
                }

                // Verify the photo actually belongs to this folder before deleting
                List<Photo> folderPhotos = photoDAO.getPhotosByFolder(folderId);
                boolean ownsPhoto = folderPhotos.stream().anyMatch(p -> p.getId() == photoId);

                if (ownsPhoto) {
                    String filePath = photoDAO.deletePhoto(photoId);
                    if (filePath != null) {
                        String realPath = getServletContext().getRealPath(filePath);
                        if (realPath != null) {
                            new File(realPath).delete();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/folder?folderId=" + folderId);
    }
}