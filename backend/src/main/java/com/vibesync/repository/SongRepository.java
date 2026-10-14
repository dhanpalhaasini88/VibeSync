package com.vibesync.repository;

import com.vibesync.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Used by Weather Vibe — finds songs that match a mood (e.g. "Calm")
    List<Song> findByMoodIgnoreCase(String mood);

    // Used by Genre filter — finds songs by genre (e.g. "Lo-Fi")
    List<Song> findByGenreIgnoreCase(String genre);

    // NEW: Used by Search — finds songs where title OR artist contains the query
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);
}
