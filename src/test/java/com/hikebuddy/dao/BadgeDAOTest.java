package com.hikebuddy.dao;

import com.hikebuddy.model.Badge;
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

public class BadgeDAOTest {

	private final BadgeDAO badgeDAO = new BadgeDAO();
	private final UserDAO userDAO = new UserDAO();

	private int testUserId;

	@BeforeEach
	void setUp() throws SQLException {
		User user = new User("badgetest_" + System.nanoTime(), "hash", "salt");
		userDAO.insert(user);
		testUserId = user.getId();
	}

	@AfterEach
	void tearDown() throws SQLException {
		try (Connection conn = DBHelper.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Badge WHERE user_id = ?")) {
				stmt.setInt(1, testUserId);
				stmt.executeUpdate();
			}
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM User WHERE id = ?")) {
				stmt.setInt(1, testUserId);
				stmt.executeUpdate();
			}
		}
	}

	@Test
	void awardIfNotExists_awardsNewBadge() throws SQLException {
		badgeDAO.awardIfNotExists(testUserId, "AMATEUR_AUTHOR");

		List<Badge> badges = badgeDAO.getByUser(testUserId);
		assertEquals(1, badges.size());
		assertEquals("AMATEUR_AUTHOR", badges.get(0).getBadgeType());
		assertEquals(testUserId, badges.get(0).getUserId());
	}

	@Test
	void awardIfNotExists_calledTwice_doesNotDuplicate() throws SQLException {
		badgeDAO.awardIfNotExists(testUserId, "AMATEUR_AUTHOR");
		badgeDAO.awardIfNotExists(testUserId, "AMATEUR_AUTHOR");

		List<Badge> badges = badgeDAO.getByUser(testUserId);
		assertEquals(1, badges.size(), "Awarding the same badge twice should not create a duplicate row");
	}

	@Test
	void awardIfNotExists_differentBadgeTypes_bothAwarded() throws SQLException {
		badgeDAO.awardIfNotExists(testUserId, "AMATEUR_AUTHOR");
		badgeDAO.awardIfNotExists(testUserId, "QUIZ_MACHINE");

		List<Badge> badges = badgeDAO.getByUser(testUserId);
		assertEquals(2, badges.size());
	}

	@Test
	void getByUser_noBadges_returnsEmptyList() throws SQLException {
		List<Badge> badges = badgeDAO.getByUser(testUserId);
		assertTrue(badges.isEmpty());
	}

	@Test
	void getByUser_returnsBadgesOrderedByEarnedAtAscending() throws SQLException, InterruptedException {
		badgeDAO.awardIfNotExists(testUserId, "FIRST_BADGE");
		Thread.sleep(1000); // ensure distinct earned_at timestamps (TIMESTAMP column has 1-second resolution)
		badgeDAO.awardIfNotExists(testUserId, "SECOND_BADGE");

		List<Badge> badges = badgeDAO.getByUser(testUserId);
		assertEquals("FIRST_BADGE", badges.get(0).getBadgeType());
		assertEquals("SECOND_BADGE", badges.get(1).getBadgeType());
	}
}