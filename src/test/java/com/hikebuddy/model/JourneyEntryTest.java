package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JourneyEntryTest {

    @Test
    void settersAndGettersRoundTripAllFields() {
        JourneyEntry entry = new JourneyEntry();
        Date date = Date.valueOf("2026-05-01");

        entry.setId(1);
        entry.setUserId(2);
        entry.setHikeRouteId(3);
        entry.setRouteName("Kazbegi Ridge");
        entry.setDate(date);
        entry.setDistance(12.5);
        entry.setDifficulty("HARD");
        entry.setStatus("COMPLETED");
        entry.setNotes("Great views");

        assertEquals(1, entry.getId());
        assertEquals(2, entry.getUserId());
        assertEquals(3, entry.getHikeRouteId());
        assertEquals("Kazbegi Ridge", entry.getRouteName());
        assertEquals(date, entry.getDate());
        assertEquals(12.5, entry.getDistance());
        assertEquals("HARD", entry.getDifficulty());
        assertEquals("COMPLETED", entry.getStatus());
        assertEquals("Great views", entry.getNotes());
    }

    @Test
    void noArgConstructorLeavesFieldsAtDefaults() {
        JourneyEntry entry = new JourneyEntry();

        assertEquals(0, entry.getId());
        assertEquals(0, entry.getUserId());
        assertEquals(0, entry.getHikeRouteId());
        assertNull(entry.getRouteName());
        assertNull(entry.getDate());
        assertEquals(0.0, entry.getDistance());
        assertNull(entry.getDifficulty());
        assertNull(entry.getStatus());
        assertNull(entry.getNotes());
    }
}
