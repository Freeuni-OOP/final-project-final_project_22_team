package com.serialtracker.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_episodes_status")
public class UserEpisodeStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int showId;
    private int seasonNumber;
    private int episodeNumber;

    public UserEpisodeStatus() {}
    public UserEpisodeStatus(Long userId, int showId, int seasonNumber, int episodeNumber) {
        this.userId = userId;
        this.showId = showId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }
    public int getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }
}