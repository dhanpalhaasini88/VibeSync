package com.vibesync.service;

import com.vibesync.entity.Playlist;
import com.vibesync.entity.Song;
import com.vibesync.entity.User;
import com.vibesync.repository.PlaylistRepository;
import com.vibesync.repository.SongRepository;
import com.vibesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all playlists for a user (with song count info)
    public List<Playlist> getPlaylists(String email) {
        User user = getUser(email);
        return playlistRepository.findByUserId(user.getId());
    }

    // Get a single playlist with full Song objects
    public Map<String, Object> getPlaylistWithSongs(String email, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        // Security: users can only access their own playlists
        User user = getUser(email);
        if (!playlist.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<Song> songs = playlist.getSongIds().stream()
                .map(songId -> songRepository.findById(songId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("id",        playlist.getId());
        response.put("name",      playlist.getName());
        response.put("createdAt", playlist.getCreatedAt());
        response.put("songs",     songs);
        return response;
    }

    // Create a new empty playlist
    public Playlist createPlaylist(String email, String name) {
        User user = getUser(email);
        Playlist playlist = new Playlist(name, user.getId());
        return playlistRepository.save(playlist);
    }

    // Add a song to a playlist
    public boolean addSongToPlaylist(String email, Long playlistId, Long songId) {
        Playlist playlist = getOwnedPlaylist(email, playlistId);

        if (playlist.getSongIds().contains(songId)) {
            return false; // already in playlist
        }

        playlist.getSongIds().add(songId);
        playlistRepository.save(playlist);
        return true;
    }

    // Remove a song from a playlist
    public boolean removeSongFromPlaylist(String email, Long playlistId, Long songId) {
        Playlist playlist = getOwnedPlaylist(email, playlistId);
        boolean removed = playlist.getSongIds().remove(songId);
        if (removed) playlistRepository.save(playlist);
        return removed;
    }

    // Delete an entire playlist
    public boolean deletePlaylist(String email, Long playlistId) {
        Playlist playlist = getOwnedPlaylist(email, playlistId);
        playlistRepository.delete(playlist);
        return true;
    }

    // Helpers
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Playlist getOwnedPlaylist(String email, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        User user = getUser(email);
        if (!playlist.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return playlist;
    }
}
