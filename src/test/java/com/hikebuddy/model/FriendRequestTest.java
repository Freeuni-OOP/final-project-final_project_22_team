package com.hikebuddy.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FriendRequestTest {

    @Test
    void settersAndGettersRoundTripAllFields() {
        FriendRequest request = new FriendRequest();
        Timestamp createdAt = Timestamp.valueOf("2026-05-01 10:00:00");
        Timestamp acceptedAt = Timestamp.valueOf("2026-05-02 11:00:00");

        request.setId(1);
        request.setSenderId(2);
        request.setReceiverId(3);
        request.setSenderUsername("hiker_bob");
        request.setReceiverUsername("hiker_alice");
        request.setStatus("PENDING");
        request.setCreatedAt(createdAt);
        request.setAcceptedAt(acceptedAt);

        assertEquals(1, request.getId());
        assertEquals(2, request.getSenderId());
        assertEquals(3, request.getReceiverId());
        assertEquals("hiker_bob", request.getSenderUsername());
        assertEquals("hiker_alice", request.getReceiverUsername());
        assertEquals("PENDING", request.getStatus());
        assertEquals(createdAt, request.getCreatedAt());
        assertEquals(acceptedAt, request.getAcceptedAt());
    }

    @Test
    void noArgConstructorLeavesFieldsAtDefaults() {
        FriendRequest request = new FriendRequest();

        assertEquals(0, request.getId());
        assertEquals(0, request.getSenderId());
        assertEquals(0, request.getReceiverId());
        assertNull(request.getSenderUsername());
        assertNull(request.getReceiverUsername());
        assertNull(request.getStatus());
        assertNull(request.getCreatedAt());
        assertNull(request.getAcceptedAt());
    }
}
