package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StoryFolderTest {

    @Test
    void settersAndGettersRoundTripAllFields() {
        StoryFolder folder = new StoryFolder();
        Timestamp createdAt = Timestamp.valueOf("2026-05-01 10:00:00");

        folder.setId(1);
        folder.setUserId(2);
        folder.setJourneyEntryId(3);
        folder.setName("Kazbegi Trip");
        folder.setDescription("Two days on the ridge");
        folder.setCreatedAt(createdAt);
        folder.setThumbnailPath("/uploads/1/photo1.jpg");

        assertEquals(1, folder.getId());
        assertEquals(2, folder.getUserId());
        assertEquals(3, folder.getJourneyEntryId());
        assertEquals("Kazbegi Trip", folder.getName());
        assertEquals("Two days on the ridge", folder.getDescription());
        assertEquals(createdAt, folder.getCreatedAt());
        assertEquals("/uploads/1/photo1.jpg", folder.getThumbnailPath());
    }

    @Test
    void journeyEntryIdCanBeNullForManuallyCreatedFolders() {
        StoryFolder folder = new StoryFolder();

        folder.setJourneyEntryId(null);

        assertNull(folder.getJourneyEntryId());
    }

    @Test
    void noArgConstructorLeavesFieldsAtDefaults() {
        StoryFolder folder = new StoryFolder();

        assertEquals(0, folder.getId());
        assertEquals(0, folder.getUserId());
        assertNull(folder.getJourneyEntryId());
        assertNull(folder.getName());
        assertNull(folder.getDescription());
        assertNull(folder.getCreatedAt());
        assertNull(folder.getThumbnailPath());
    }
}
