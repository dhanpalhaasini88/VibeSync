package com.vibesync.controller;

import com.vibesync.entity.Song;
import com.vibesync.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FavoriteController — manage a user's saved favorite songs.
 *
 * All routes require a valid JWT in the Authorization header:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * Routes:
 *   GET    /api/favorites          → get all your favorite songs
 *   POST   /api/favorites/{songId} → add a song to favorites
 *   DELETE /api/favorites/{songId} → remove a song from favorites
 */
@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // GET /api/favorites → returns list of full Song objects
    @GetMapping
    public List<Song> getFavorites(Authentication auth) {
        String email = auth.getName(); // email extracted from JWT by JwtFilter
        return favoriteService.getFavoriteSongs(email);
    }

    // POST /api/favorites/{songId} → add song to favorites
    @PostMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> addFavorite(@PathVariable Long songId,
                                                            Authentication auth) {
        String email = auth.getName();
        boolean added = favoriteService.addFavorite(email, songId);

        if (added) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Song added to favorites!"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Song is already in your favorites."
            ));
        }
    }

    // DELETE /api/favorites/{songId} → remove song from favorites
    @DeleteMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long songId,
                                                               Authentication auth) {
        String email = auth.getName();
        boolean removed = favoriteService.removeFavorite(email, songId);

        if (removed) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Song removed from favorites."
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Song was not in your favorites."
            ));
        }
    }
}
