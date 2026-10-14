package com.vibesync.entity;

import jakarta.persistence.*;

/**
 * Favorite — links a User to a Song they've liked.
 * One user can have many favorites; each favorite belongs to one user+song pair.
 */
@Entity
@Table(name = "favorites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "song_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    // Default constructor required by JPA
    public Favorite() {}

    public Favorite(Long userId, Long songId) {
        this.userId = userId;
        this.songId = songId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
}
