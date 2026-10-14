package com.vibesync.controller;

import com.vibesync.entity.Song;
import com.vibesync.service.GeminiRecommendationService;
import com.vibesync.service.SongService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AiRecommendationController {

    private static final Set<String> AVAILABLE_MOODS = Set.of(
            "Calm", "Energetic", "Cozy", "Melancholic", "Romantic", "Focused", "Happy", "Night"
    );

    private final GeminiRecommendationService geminiService;
    private final SongService songService;

    public AiRecommendationController(GeminiRecommendationService geminiService, SongService songService) {
        this.geminiService = geminiService;
        this.songService = songService;
    }

    @PostMapping("/api/ai/vibe-pick")
    public Map<String, Object> getVibePick(@RequestBody Map<String, String> request) throws Exception {
        String mood = request.getOrDefault("mood", "").trim();
        if (!AVAILABLE_MOODS.contains(mood)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a mood from the available list.");
        }

        List<Song> songs = songService.getAllSongs();
        return geminiService.suggestSong(mood, songs);
    }
}