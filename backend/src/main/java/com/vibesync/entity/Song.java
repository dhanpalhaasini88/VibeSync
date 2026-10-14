package com.vibesync.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;
    private String genre;
    private String language;

    @Column(name = "audio_url", nullable = false, length = 1000)
    private String audioUrl;

    @Column(name = "cover_url", length = 1000)
    private String coverUrl;

    private String mood;
    private String energy;

    // 1. Default Constructor (Required by JPA)
    public Song() {
    }

    // 2. Parameterized Constructor
    public Song(String title, String artist, String album, String genre, String language, 
                String audioUrl, String coverUrl, String mood, String energy) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.language = language;
        this.audioUrl = audioUrl;
        this.coverUrl = coverUrl;
        this.mood = mood;
        this.energy = energy;
    }

    // 3. Getters and Setters (Compact & Clean)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getEnergy() { return energy; }
    public void setEnergy(String energy) { this.energy = energy; }
}

