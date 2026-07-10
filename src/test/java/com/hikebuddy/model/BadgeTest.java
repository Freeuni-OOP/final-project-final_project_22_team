package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class BadgeTest {

	@Test
	void gettersAndSetters_workCorrectly() {
		Badge badge = new Badge();
		Timestamp now = new Timestamp(System.currentTimeMillis());

		badge.setId(1);
		badge.setUserId(42);
		badge.setBadgeType(Badge.FIRST_HIKE);
		badge.setEarnedAt(now);

		assertEquals(1, badge.getId());
		assertEquals(42, badge.getUserId());
		assertEquals(Badge.FIRST_HIKE, badge.getBadgeType());
		assertEquals(now, badge.getEarnedAt());
	}

	@Test
	void getDisplayName_delegatesToDisplayNameFor() {
		Badge badge = new Badge();
		badge.setBadgeType(Badge.TEN_HIKES);

		assertEquals(Badge.displayNameFor(Badge.TEN_HIKES), badge.getDisplayName());
	}

	@Test
	void displayNameFor_knownTypes_returnsHumanReadableNames() {
		assertEquals("First Hike 🥾", Badge.displayNameFor(Badge.FIRST_HIKE));
		assertEquals("10 Hikes 🏔️", Badge.displayNameFor(Badge.TEN_HIKES));
		assertEquals("First Friend 🤝", Badge.displayNameFor(Badge.FIRST_FRIEND));
		assertEquals("Gear Collector 🎒", Badge.displayNameFor(Badge.GEAR_COLLECTOR));
	}

	@Test
	void displayNameFor_unknownType_returnsInputUnchanged() {
		assertEquals("SOME_UNKNOWN_TYPE", Badge.displayNameFor("SOME_UNKNOWN_TYPE"));
	}

	@Test
	void requirementFor_knownTypes_returnsCorrectDescriptions() {
		assertEquals("Complete your first hike", Badge.requirementFor(Badge.FIRST_HIKE));
		assertEquals("Complete 10 hikes", Badge.requirementFor(Badge.TEN_HIKES));
		assertEquals("Make your first friend", Badge.requirementFor(Badge.FIRST_FRIEND));
		assertEquals("Add 5 gear items to your list", Badge.requirementFor(Badge.GEAR_COLLECTOR));
	}

	@Test
	void requirementFor_unknownType_returnsEmptyString() {
		assertEquals("", Badge.requirementFor("SOME_UNKNOWN_TYPE"));
	}

	@Test
	void allTypes_containsExactlyFourKnownBadgeTypes() {
		assertEquals(4, Badge.ALL_TYPES.length);
		assertEquals(Badge.FIRST_HIKE, Badge.ALL_TYPES[0]);
		assertEquals(Badge.TEN_HIKES, Badge.ALL_TYPES[1]);
		assertEquals(Badge.FIRST_FRIEND, Badge.ALL_TYPES[2]);
		assertEquals(Badge.GEAR_COLLECTOR, Badge.ALL_TYPES[3]);
	}
}