package com.vibesync.controller;

import com.vibesync.entity.Playlist;
import com.vibesync.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PlaylistController — full playlist CRUD.
 *
 * All routes require a valid JWT token.
 *
 * Routes:
 *   GET    /api/playlists                         → get all your playlists
 *   POST   /api/playlists                         → create new playlist  { "name": "Chill Mix" }
 *   GET    /api/playlists/{id}                    → get playlist with full song objects
 *   DELETE /api/playlists/{id}                    → delete a playlist
 *   POST   /api/playlists/{id}/songs/{songId}     → add song to playlist
 *   DELETE /api/playlists/{id}/songs/{songId}     → remove song from playlist
 */
@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "http://localhost:5173")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @GetMapping
    public List<Playlist> getPlaylists(Authentication auth) {
        return playlistService.getPlaylists(auth.getName());
    }

    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@RequestBody Map<String, String> body,
                                                    Authentication auth) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Playlist created = playlistService.createPlaylist(auth.getName(), name);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylist(@PathVariable Long id,
                                                            Authentication auth) {
        try {
            Map<String, Object> playlist = playlistService.getPlaylistWithSongs(auth.getName(), id);
            return ResponseEntity.ok(playlist);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlaylist(@PathVariable Long id,
                                                               Authentication auth) {
        playlistService.deletePlaylist(auth.getName(), id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Playlist deleted."));
    }

    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> addSong(@PathVariable Long id,
                                                        @PathVariable Long songId,
                                                        Authentication auth) {
        boolean added = playlistService.addSongToPlaylist(auth.getName(), id, songId);
        return ResponseEntity.ok(Map.of(
            "success", added,
            "message", added ? "Song added to playlist!" : "Song already in playlist."
        ));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> removeSong(@PathVariable Long id,
                                                           @PathVariable Long songId,
                                                           Authentication auth) {
        boolean removed = playlistService.removeSongFromPlaylist(auth.getName(), id, songId);
        return ResponseEntity.ok(Map.of(
            "success", removed,
            "message", removed ? "Song removed from playlist." : "Song not found in playlist."
        ));
    }
}
