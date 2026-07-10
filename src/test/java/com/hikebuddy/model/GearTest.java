package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class GearTest {

    @Test
    void settersAndGettersRoundTripAllFields() {
        Gear gear = new Gear();

        gear.setId(1);
        gear.setUserId(2);
        gear.setName("Trekking Poles");
        gear.setChecked(true);

        assertEquals(1, gear.getId());
        assertEquals(2, gear.getUserId());
        assertEquals("Trekking Poles", gear.getName());
        assertEquals(true, gear.isChecked());
    }

    @Test
    void noArgConstructorLeavesFieldsAtDefaults() {
        Gear gear = new Gear();

        assertEquals(0, gear.getId());
        assertEquals(0, gear.getUserId());
        assertNull(gear.getName());
        assertFalse(gear.isChecked());
    }
}
