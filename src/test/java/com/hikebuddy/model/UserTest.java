package com.hikebuddy.model;

import org.junit.Test;
import java.sql.Timestamp;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.AssertJUnit.assertNull;

public class UserTest {

    @Test
    public void testEmptyConstructor() {
        User user = new User();
        assertEquals(0, user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPasswordHash());
        assertNull(user.getSalt());
        assertNull(user.getHikingLevel());
        assertNull(user.getBio());
        assertNull(user.getCreatedAt());
    }

    @Test
    public void testConvenienceConstructor() {
        User user = new User("tornike", "hashedpw", "randomsalt");
        assertEquals("tornike", user.getUsername());
        assertEquals("hashedpw", user.getPasswordHash());
        assertEquals("randomsalt", user.getSalt());
        assertEquals("BEGINNER", user.getHikingLevel());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        user.setId(1);
        user.setUsername("testuser");
        user.setPasswordHash("hash123");
        user.setSalt("salt123");
        user.setHikingLevel("INTERMEDIATE");
        user.setBio("I love hiking");
        user.setCreatedAt(now);

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals("salt123", user.getSalt());
        assertEquals("INTERMEDIATE", user.getHikingLevel());
        assertEquals("I love hiking", user.getBio());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    public void testHikingLevelValues() {
        User user = new User();

        user.setHikingLevel("BEGINNER");
        assertEquals("BEGINNER", user.getHikingLevel());

        user.setHikingLevel("INTERMEDIATE");
        assertEquals("INTERMEDIATE", user.getHikingLevel());

        user.setHikingLevel("ADVANCED");
        assertEquals("ADVANCED", user.getHikingLevel());
    }

    @Test
    public void testBioCanBeNull() {
        User user = new User();
        user.setBio(null);
        assertNull(user.getBio());
    }

    @Test
    public void testToString() {
        User user = new User();
        user.setId(1);
        user.setUsername("tornike");
        user.setHikingLevel("BEGINNER");
        String result = user.toString();
        assertTrue(result.contains("tornike"));
        assertTrue(result.contains("BEGINNER"));
    }
}