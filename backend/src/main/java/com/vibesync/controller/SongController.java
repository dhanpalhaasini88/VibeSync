package com.vibesync.controller;

import com.vibesync.entity.Song;
import com.vibesync.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "http://localhost:5173") // Allows React to talk to this API
public class SongController {

    @Autowired
    private SongService songService;

    // 1. GET http://localhost:8081/api/songs -> Returns all songs
    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    // 2. GET http://localhost:8081/api/songs/{id} -> Returns one song by ID
    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        return songService.getSongById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. GET http://localhost:8081/api/songs/mood/{mood} -> For Weather Vibe!
    @GetMapping("/mood/{mood}")
    public List<Song> getSongsByMood(@PathVariable String mood) {
        return songService.getSongsByMood(mood);
    }

    // 4. NEW: GET http://localhost:8081/api/songs/genre/{genre} -> Filter by genre
    @GetMapping("/genre/{genre}")
    public List<Song> getSongsByGenre(@PathVariable String genre) {
        return songService.getSongsByGenre(genre);
    }

    // 5. NEW: GET http://localhost:8081/api/songs/search?q=rain -> Search by title or artist
    @GetMapping("/search")
    public List<Song> searchSongs(@RequestParam String q) {
        return songService.searchSongs(q);
    }

    // 6. POST http://localhost:8081/api/songs -> Adds a new song
    @PostMapping
    public Song addSong(@RequestBody Song song) {
        return songService.saveSong(song);
    }
}
