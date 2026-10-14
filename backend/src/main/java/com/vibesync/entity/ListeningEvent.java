package com.vibesync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ListeningEvent — records every interaction a user has with a song.
 *
 * Event types:
 *   PLAY     → user started playing the song
 *   COMPLETE → user listened to the whole song (not skipped)
 *   SKIP     → user skipped the song before it finished
 *   LIKE     → user clicked the like/favorite button
 *   REPLAY   → user replayed the song immediately
 */
@Entity
@Table(name = "listening_events")
public class ListeningEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // PLAY, COMPLETE, SKIP, LIKE, REPLAY

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA requires a no-arg constructor
    public ListeningEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public ListeningEvent(Long userId, Long songId, String eventType) {
        this.userId = userId;
        this.songId = songId;
        this.eventType = eventType;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
