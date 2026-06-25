package com.hikebuddy.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {

    private static final int SALT_LENGTH_BYTES = 16;

    // Generates a new random salt, hex-encoded for storage
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    // Hashes plaintext with the given salt (salt + plaintext), hex-encoded
    public static String hashPassword(String plaintext, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = digest.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // Verifies plaintext against a stored hash + salt, using constant-time comparison
    public static boolean verifyPassword(String plaintext, String storedHash, String storedSalt) {
        String computedHash = hashPassword(plaintext, storedSalt);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Helper: converts a byte array into a lowercase hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}