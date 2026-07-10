package com.hikebuddy.dao;

import com.hikebuddy.model.Photo;
import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PhotoDAOTest {

	private final PhotoDAO photoDAO = new PhotoDAO();
	private final UserDAO userDAO = new UserDAO();
	private final StoryFolderDAO storyFolderDAO = new StoryFolderDAO();

	private int testUserId;
	private int testFolderId;

	@BeforeEach
	void setUp() throws SQLException {
		// Photo -> StoryFolder -> User foreign key chain, so both must exist first
		User user = new User("phototest_" + System.nanoTime(), "hash", "salt");
		userDAO.insert(user);
		testUserId = user.getId();

		testFolderId = storyFolderDAO.createFolder(testUserId, null, "Test Folder");
	}

	@AfterEach
	void tearDown() throws SQLException {
		// Clean up children first, due to foreign key constraints
		try (Connection conn = DBHelper.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Photo WHERE folder_id = ?")) {
				stmt.setInt(1, testFolderId);
				stmt.executeUpdate();
			}
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM StoryFolder WHERE id = ?")) {
				stmt.setInt(1, testFolderId);
				stmt.executeUpdate();
			}
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM User WHERE id = ?")) {
				stmt.setInt(1, testUserId);
				stmt.executeUpdate();
			}
		}
	}

	@Test
	void addPhoto_insertsRowSuccessfully() throws SQLException {
		photoDAO.addPhoto(testFolderId, "/uploads/test1.jpg");

		List<Photo> photos = photoDAO.getPhotosByFolder(testFolderId);
		assertEquals(1, photos.size());
		assertEquals("/uploads/test1.jpg", photos.get(0).getFilePath());
		assertEquals(testFolderId, photos.get(0).getFolderId());
	}

	@Test
	void getPhotosByFolder_returnsAllPhotosInFolder() throws SQLException {
		photoDAO.addPhoto(testFolderId, "/uploads/first.jpg");
		photoDAO.addPhoto(testFolderId, "/uploads/second.jpg");

		List<Photo> photos = photoDAO.getPhotosByFolder(testFolderId);
		assertEquals(2, photos.size());
	}

	@Test
	void getPhotosByFolder_emptyFolder_returnsEmptyList() throws SQLException {
		List<Photo> photos = photoDAO.getPhotosByFolder(testFolderId);
		assertTrue(photos.isEmpty());
	}

	@Test
	void deletePhoto_returnsFilePathAndRemovesRow() throws SQLException {
		photoDAO.addPhoto(testFolderId, "/uploads/todelete.jpg");
		int photoId = photoDAO.getPhotosByFolder(testFolderId).get(0).getId();

		String returnedPath = photoDAO.deletePhoto(photoId);

		assertEquals("/uploads/todelete.jpg", returnedPath);
		assertTrue(photoDAO.getPhotosByFolder(testFolderId).isEmpty());
	}

	@Test
	void deletePhoto_nonExistentId_returnsNull() throws SQLException {
		String result = photoDAO.deletePhoto(999999);
		assertNull(result);
	}

	@Test
	void deleteAllInFolder_removesAllPhotos() throws SQLException {
		photoDAO.addPhoto(testFolderId, "/uploads/a.jpg");
		photoDAO.addPhoto(testFolderId, "/uploads/b.jpg");
		photoDAO.addPhoto(testFolderId, "/uploads/c.jpg");

		photoDAO.deleteAllInFolder(testFolderId);

		assertTrue(photoDAO.getPhotosByFolder(testFolderId).isEmpty());
	}
}