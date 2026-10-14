package com.vibesync.service;

import com.vibesync.entity.Favorite;
import com.vibesync.entity.Song;
import com.vibesync.entity.User;
import com.vibesync.repository.FavoriteRepository;
import com.vibesync.repository.SongRepository;
import com.vibesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all favorite songs for a user (returns full Song objects, not just IDs)
    public List<Song> getFavoriteSongs(String email) {
        User user = getUserByEmail(email);

        List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());

        // Convert list of Favorite (which holds song IDs) into actual Song objects
        return favorites.stream()
                .map(fav -> songRepository.findById(fav.getSongId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // Add a song to favorites (returns false if already favorited)
    public boolean addFavorite(String email, Long songId) {
        User user = getUserByEmail(email);

        if (favoriteRepository.existsByUserIdAndSongId(user.getId(), songId)) {
            return false; // already favorited
        }

        favoriteRepository.save(new Favorite(user.getId(), songId));
        return true;
    }

    // Remove a song from favorites
    public boolean removeFavorite(String email, Long songId) {
        User user = getUserByEmail(email);

        Optional<Favorite> fav = favoriteRepository.findByUserIdAndSongId(user.getId(), songId);
        if (fav.isEmpty()) {
            return false; // wasn't favorited
        }

        favoriteRepository.delete(fav.get());
        return true;
    }

    // Check if a specific song is in the user's favorites
    public boolean isFavorite(String email, Long songId) {
        User user = getUserByEmail(email);
        return favoriteRepository.existsByUserIdAndSongId(user.getId(), songId);
    }

    // Helper method: get user by email (email is stored in the JWT token)
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
