package com.hikebuddy.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HikeRouteTest {

    @Test
    void testEmptyConstructor() {
        HikeRoute route = new HikeRoute();
        assertEquals(0, route.getId());
        assertNull(route.getName());
        assertNull(route.getRegion());
        assertNull(route.getDifficulty());
        assertEquals(0.0, route.getDistance(), 0.001);
        assertNull(route.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        HikeRoute route = new HikeRoute();

        route.setId(1);
        route.setName("Kazbegi Trail");
        route.setRegion("Kazbegi");
        route.setDifficulty("HARD");
        route.setDistance(12.5);
        route.setDescription("A challenging mountain trail");

        assertEquals(1, route.getId());
        assertEquals("Kazbegi Trail", route.getName());
        assertEquals("Kazbegi", route.getRegion());
        assertEquals("HARD", route.getDifficulty());
        assertEquals(12.5, route.getDistance(), 0.001);
        assertEquals("A challenging mountain trail", route.getDescription());
    }

    @Test
    void testDifficultyValues() {
        HikeRoute route = new HikeRoute();

        route.setDifficulty("EASY");
        assertEquals("EASY", route.getDifficulty());

        route.setDifficulty("MEDIUM");
        assertEquals("MEDIUM", route.getDifficulty());

        route.setDifficulty("HARD");
        assertEquals("HARD", route.getDifficulty());
    }

    @Test
    void testDistancePrecision() {
        HikeRoute route = new HikeRoute();
        route.setDistance(5.75);
        assertEquals(5.75, route.getDistance(), 0.001);
    }

    @Test
    void testDescriptionCanBeNull() {
        HikeRoute route = new HikeRoute();
        route.setDescription(null);
        assertNull(route.getDescription());
    }
}