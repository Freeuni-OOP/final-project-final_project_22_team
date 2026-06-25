package com.hikebuddy.model;

import java.sql.Timestamp;

/**
 * Represents one row from the User table in the database.
 * This is a plain data class — no SQL, no HTTP.
 * Other classes (UserDAO, LoginServlet, etc.) create and pass around User objects.
 */
public class User {

    // These fields match the columns in the User table from schema.sql:
    // id INT AUTO_INCREMENT PRIMARY KEY
    private int id;

    // username VARCHAR(50) UNIQUE NOT NULL
    private String username;

    // password_hash VARCHAR(255) NOT NULL
    // We store the HASH, never the real password
    private String passwordHash;

    // salt VARCHAR(100) NOT NULL
    // A random string mixed in before hashing, unique per user
    private String salt;

    // hiking_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED')
    private String hikingLevel;

    // bio TEXT (can be null — users may not have written a bio yet)
    private String bio;

    // created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    private Timestamp createdAt;

    // ---------------------------------------------------------------
    // CONSTRUCTORS
    // ---------------------------------------------------------------

    /**
     * Empty constructor — needed when UserDAO reads a row from the DB
     * and fills in fields one by one using setters.
     */
    public User() {
    }

    /**
     * Convenience constructor for registration:
     * when a new user signs up, we know username, hash, and salt right away.
     * id and createdAt are set by the database automatically.
     */
    public User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.hikingLevel = "BEGINNER"; // default for all new users
    }

    // ---------------------------------------------------------------
    // GETTERS AND SETTERS
    // Getters let other classes READ a field.
    // Setters let other classes WRITE to a field.
    // This is standard Java OOP — direct field access (user.id) is bad practice.
    // ---------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHikingLevel() {
        return hikingLevel;
    }

    public void setHikingLevel(String hikingLevel) {
        this.hikingLevel = hikingLevel;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // ---------------------------------------------------------------
    // toString — useful for debugging (printing a User object shows its data)
    // ---------------------------------------------------------------

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', hikingLevel='" + hikingLevel + "'}";
    }
}
