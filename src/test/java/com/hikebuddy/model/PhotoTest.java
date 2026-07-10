package com.hikebuddy.model;

import org.junit.Test;
import java.sql.Timestamp;
import static org.junit.Assert.*;

public class PhotoTest {

    @Test
    public void testEmptyConstructor() {
        Photo photo = new Photo();
        assertEquals(0, photo.getId());
        assertEquals(0, photo.getFolderId());
        assertNull(photo.getFilePath());
        assertNull(photo.getUploadedAt());
    }

    @Test
    public void testSettersAndGetters() {
        Photo photo = new Photo();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        photo.setId(1);
        photo.setFolderId(5);
        photo.setFilePath("/uploads/photo.jpg");
        photo.setUploadedAt(now);

        assertEquals(1, photo.getId());
        assertEquals(5, photo.getFolderId());
        assertEquals("/uploads/photo.jpg", photo.getFilePath());
        assertEquals(now, photo.getUploadedAt());
    }

    @Test
    public void testFilePathCanBeNull() {
        Photo photo = new Photo();
        photo.setFilePath(null);
        assertNull(photo.getFilePath());
    }

    @Test
    public void testFilePathValues() {
        Photo photo = new Photo();

        photo.setFilePath("/uploads/abc123.jpg");
        assertEquals("/uploads/abc123.jpg", photo.getFilePath());

        photo.setFilePath("/uploads/xyz789.png");
        assertEquals("/uploads/xyz789.png", photo.getFilePath());
    }

    @Test
    public void testFolderIdAssignment() {
        Photo photo = new Photo();
        photo.setFolderId(42);
        assertEquals(42, photo.getFolderId());
    }
}