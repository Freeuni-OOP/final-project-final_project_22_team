package com.hikebuddy.dao;

import com.hikebuddy.model.User;
import com.hikebuddy.util.DBHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarDAOTest {

	private final CalendarDAO calendarDAO = new CalendarDAO();
	private final UserDAO userDAO = new UserDAO();

	private int testUserId;

	@BeforeEach
	void setUp() throws SQLException {
		User user = new User("caltest_" + System.nanoTime(), "hash", "salt");
		userDAO.insert(user);
		testUserId = user.getId();
	}

	@AfterEach
	void tearDown() throws SQLException {
		try (Connection conn = DBHelper.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM AvailableDay WHERE user_id = ?")) {
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
	void toggleDay_firstCall_marksDayAvailable() throws SQLException {
		LocalDate date = LocalDate.of(2026, 7, 15);

		calendarDAO.toggleDay(testUserId, date);

		Set<Integer> availableDays = calendarDAO.getAvailableDays(testUserId, 2026, 7);
		assertTrue(availableDays.contains(15));
	}

	@Test
	void toggleDay_secondCall_unmarksDay() throws SQLException {
		LocalDate date = LocalDate.of(2026, 7, 15);

		calendarDAO.toggleDay(testUserId, date); // mark
		calendarDAO.toggleDay(testUserId, date); // unmark

		Set<Integer> availableDays = calendarDAO.getAvailableDays(testUserId, 2026, 7);
		assertFalse(availableDays.contains(15));
	}

	@Test
	void getAvailableDays_multipleDays_returnsAllForCorrectMonth() throws SQLException {
		calendarDAO.toggleDay(testUserId, LocalDate.of(2026, 7, 5));
		calendarDAO.toggleDay(testUserId, LocalDate.of(2026, 7, 12));
		calendarDAO.toggleDay(testUserId, LocalDate.of(2026, 7, 20));

		Set<Integer> availableDays = calendarDAO.getAvailableDays(testUserId, 2026, 7);
		assertEquals(3, availableDays.size());
		assertTrue(availableDays.contains(5));
		assertTrue(availableDays.contains(12));
		assertTrue(availableDays.contains(20));
	}

	@Test
	void getAvailableDays_filtersOutOtherMonths() throws SQLException {
		calendarDAO.toggleDay(testUserId, LocalDate.of(2026, 7, 15));
		calendarDAO.toggleDay(testUserId, LocalDate.of(2026, 8, 15)); // different month

		Set<Integer> julyDays = calendarDAO.getAvailableDays(testUserId, 2026, 7);
		assertEquals(1, julyDays.size());
		assertTrue(julyDays.contains(15));
	}

	@Test
	void getAvailableDays_noneMarked_returnsEmptySet() throws SQLException {
		Set<Integer> availableDays = calendarDAO.getAvailableDays(testUserId, 2026, 7);
		assertTrue(availableDays.isEmpty());
	}
}