package com.vibesync.repository;

import com.vibesync.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // Get all playlists for a specific user
    List<Playlist> findByUserId(Long userId);
}
