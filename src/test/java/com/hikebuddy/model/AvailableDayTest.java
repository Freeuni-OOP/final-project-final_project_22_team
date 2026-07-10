package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

public class AvailableDayTest {

	@Test
	void gettersAndSetters_workCorrectly() {
		AvailableDay day = new AvailableDay();
		Date date = Date.valueOf("2026-07-15");

		day.setId(1);
		day.setUserId(42);
		day.setAvailableDate(date);

		assertEquals(1, day.getId());
		assertEquals(42, day.getUserId());
		assertEquals(date, day.getAvailableDate());
	}
}