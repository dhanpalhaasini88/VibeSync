package com.vibesync.service;

import com.vibesync.entity.Song;
import com.vibesync.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    // 1. Fetch every song in the database
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // 2. Fetch one song by its unique ID
    public Optional<Song> getSongById(Long id) {
        return songRepository.findById(id);
    }

    // 3. Fetch songs matching a mood (used by Weather Vibe)
    public List<Song> getSongsByMood(String mood) {
        return songRepository.findByMoodIgnoreCase(mood);
    }

    // 4. Fetch songs matching a genre
    public List<Song> getSongsByGenre(String genre) {
        return songRepository.findByGenreIgnoreCase(genre);
    }

    // 5. Save a song to the database
    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

    // 6. NEW: Search songs by title OR artist keyword
    public List<Song> searchSongs(String query) {
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(query, query);
    }
}
