package com.vibesync.repository;

import com.vibesync.entity.ListeningEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningEventRepository extends JpaRepository<ListeningEvent, Long> {

    // Get all events for a user (used by recommendation algorithm)
    List<ListeningEvent> findByUserId(Long userId);

    // Get all events for a user and specific event type (e.g. all SKIPs)
    List<ListeningEvent> findByUserIdAndEventType(Long userId, String eventType);

    // Count how many times a user played a specific song
    long countByUserIdAndSongIdAndEventType(Long userId, Long songId, String eventType);
}
