package com.vibesync.controller;

import com.vibesync.entity.Song;
import com.vibesync.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RecommendationController — manages listening events and Never Skip recommendations.
 *
 * Routes:
 *   POST /api/events                        → record a listening event (requires JWT)
 *   GET  /api/recommendations/never-skip    → get personalized song list (requires JWT)
 *
 * How to record a listening event:
 *   POST /api/events
 *   Body: { "songId": 5, "eventType": "SKIP" }
 *
 *   Valid event types: PLAY, COMPLETE, SKIP, LIKE, REPLAY
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    // POST /api/events → record what the user did with a song
    @PostMapping("/api/events")
    public ResponseEntity<Map<String, Object>> recordEvent(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        String email     = auth.getName();
        Long songId      = Long.valueOf(body.get("songId").toString());
        String eventType = body.get("eventType").toString();

        // Validate event type
        List<String> validTypes = List.of("PLAY", "COMPLETE", "SKIP", "LIKE", "REPLAY");
        if (!validTypes.contains(eventType.toUpperCase())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid event type. Must be one of: PLAY, COMPLETE, SKIP, LIKE, REPLAY"
            ));
        }

        recommendationService.recordEvent(email, songId, eventType);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Event recorded: " + eventType + " for song " + songId
        ));
    }

    // GET /api/recommendations/never-skip → personalized song recommendations
    @GetMapping("/api/recommendations/never-skip")
    public List<Song> getNeverSkipRecommendations(Authentication auth) {
        String email = auth.getName();
        return recommendationService.getNeverSkipRecommendations(email);
    }
}
