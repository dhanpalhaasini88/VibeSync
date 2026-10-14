package com.vibesync.repository;

import com.vibesync.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Get all favorites for a specific user
    List<Favorite> findByUserId(Long userId);

    // Check if a user has already favorited a specific song
    boolean existsByUserIdAndSongId(Long userId, Long songId);

    // Find a specific favorite to delete it
    Optional<Favorite> findByUserIdAndSongId(Long userId, Long songId);
}
