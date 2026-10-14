package com.vibesync.service;

import com.vibesync.entity.ListeningEvent;
import com.vibesync.entity.Song;
import com.vibesync.entity.User;
import com.vibesync.repository.ListeningEventRepository;
import com.vibesync.repository.SongRepository;
import com.vibesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ListeningEventRepository eventRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Record a listening event for a user.
     * Called every time the user plays, skips, likes, completes, or replays a song.
     */
    public void recordEvent(String email, Long songId, String eventType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        ListeningEvent event = new ListeningEvent(user.getId(), songId, eventType.toUpperCase());
        eventRepository.save(event);
    }

    /**
     * Never Skip Algorithm:
     *
     * For each song the user has interacted with, calculate a "score":
     *   + 3 points  for COMPLETE (listened all the way through)
     *   + 2 points  for REPLAY   (loved it enough to replay)
     *   + 2 points  for LIKE     (explicitly liked it)
     *   + 1 point   for PLAY     (started playing)
     *   - 3 points  for SKIP     (actively skipped it)
     *
     * Songs with score >= 1 are returned, sorted by score (best first).
     * Songs with score < 1 (i.e., mostly skipped) are excluded.
     *
    * If the user has no listening history yet, returns no recommendations.
     */
    public List<Song> getNeverSkipRecommendations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        List<ListeningEvent> allEvents = eventRepository.findByUserId(user.getId());

        // Recommendations should be based on the user's actual listening history.
        if (allEvents.isEmpty()) {
            return List.of();
        }

        // Build a score map: songId → score
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (ListeningEvent event : allEvents) {
            Long songId = event.getSongId();
            int points = switch (event.getEventType()) {
                case "COMPLETE" -> 3;
                case "REPLAY"   -> 2;
                case "LIKE"     -> 2;
                case "PLAY"     -> 1;
                case "SKIP"     -> -3;
                default         -> 0;
            };
            scoreMap.merge(songId, points, Integer::sum);
        }

        // Filter out songs with score < 1 (mostly skipped)
        List<Long> recommendedSongIds = scoreMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= 1)
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Fetch and return the actual Song objects in score order
        return recommendedSongIds.stream()
                .map(songId -> songRepository.findById(songId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
