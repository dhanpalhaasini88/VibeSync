package com.vibesync.config;

import com.vibesync.entity.Song;
import com.vibesync.repository.SongRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(SongRepository songRepository) {
        return args -> {
            // Only seed if the database is currently empty
            if (songRepository.count() == 0) {
                List<Song> initialSongs = Arrays.asList(

                    // ── CALM mood (for rainy/cold/peaceful weather) ──────────────────
                    new Song(
                        "Midnight Rain", "Aura Lofi", "Late Night Vibes", "Lo-Fi", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                        "Calm", "Low"
                    ),
                    new Song(
                        "Gentle Breeze", "Luna Mist", "Quiet Moments", "Chillout", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                        "https://images.unsplash.com/photo-1470240731273-7821a6eeb6bd?w=500",
                        "Calm", "Low"
                    ),
                    new Song(
                        "Ocean Drift", "Sea Waves", "Blue Horizon", "Ambient", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                        "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=500",
                        "Calm", "Low"
                    ),
                    new Song(
                        "Rainy Afternoon", "Cloud Piano", "Drizzle Days", "Classical", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
                        "https://images.unsplash.com/photo-1485546246426-74dc88dec4d9?w=500",
                        "Calm", "Low"
                    ),
                    new Song(
                        "Still Waters", "Zen Garden", "Inner Peace", "Meditation", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
                        "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500",
                        "Calm", "Low"
                    ),

                    // ── ENERGETIC mood (for sunny/warm/clear weather) ─────────────────
                    new Song(
                        "Morning Sunshine", "Solar Beats", "Dawn Horizon", "Acoustic", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500",
                        "Energetic", "High"
                    ),
                    new Song(
                        "Neon Highway", "Cyber Drift", "Synth City", "Synthwave", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500",
                        "Energetic", "High"
                    ),
                    new Song(
                        "Power Up", "Beat Force", "Ignition", "Electronic", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                        "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500",
                        "Energetic", "High"
                    ),
                    new Song(
                        "Sunrise Run", "Cardio Kings", "Morning Rush", "Pop", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                        "https://images.unsplash.com/photo-1470770903676-69b98201ea1c?w=500",
                        "Energetic", "High"
                    ),
                    new Song(
                        "Festival Fire", "Crowd Rockers", "Summer Blaze", "EDM", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                        "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
                        "Energetic", "High"
                    ),

                    // ── COZY mood (for cloudy/autumn/cool weather) ────────────────────
                    new Song(
                        "Cozy Fireplace", "Echo Wood", "Autumn Leaves", "Ambient", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        "https://images.unsplash.com/photo-1542224566-6e85f2e6772f?w=500",
                        "Cozy", "Low"
                    ),
                    new Song(
                        "Coffeehouse Groove", "Velvet Trio", "Sunday Roast", "Jazz", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                        "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
                        "Cozy", "Medium"
                    ),
                    new Song(
                        "Warm Blanket", "Indie Folk", "Sweater Weather", "Folk", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                        "https://images.unsplash.com/photo-1516912481808-3406841bd33c?w=500",
                        "Cozy", "Low"
                    ),
                    new Song(
                        "Autumn Walk", "Maple Keys", "October Light", "Acoustic", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                        "https://images.unsplash.com/photo-1476703993599-0035a21b17a9?w=500",
                        "Cozy", "Medium"
                    ),
                    new Song(
                        "Porch Swing", "Cider House", "Comfort Zone", "Country", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
                        "https://images.unsplash.com/photo-1501426026826-31c667bdf23d?w=500",
                        "Cozy", "Low"
                    ),

                    // ── MELANCHOLIC mood (for stormy/gloomy weather) ──────────────────
                    new Song(
                        "Grey Skies", "The Sad Boys", "Overcast", "Indie", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                        "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?w=500",
                        "Melancholic", "Low"
                    ),
                    new Song(
                        "Faded Memories", "Hollow Hearts", "Empty Rooms", "Alternative", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        "https://images.unsplash.com/photo-1462275646964-a0e3386b89fa?w=500",
                        "Melancholic", "Low"
                    ),
                    new Song(
                        "Lonesome Road", "Dusty Plains", "Dusk Till Dawn", "Blues", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                        "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=500",
                        "Melancholic", "Medium"
                    ),
                    new Song(
                        "Broken Strings", "Ember & Ash", "Afterglow", "Acoustic", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500",
                        "Melancholic", "Low"
                    ),

                    // ── ROMANTIC mood (for mild spring / evening weather) ─────────────
                    new Song(
                        "Starlit Dance", "Rosa Vega", "Velvet Night", "R&B", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        "https://images.unsplash.com/photo-1465101162946-4377e57745c3?w=500",
                        "Romantic", "Medium"
                    ),
                    new Song(
                        "Candlelight", "Mellow Gold", "Dinner for Two", "Jazz", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                        "Romantic", "Low"
                    ),
                    new Song(
                        "Paris in Spring", "Chloe Martin", "Fleur", "French Pop", "French",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                        "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=500",
                        "Romantic", "Medium"
                    ),

                    // ── FOCUSED mood (for cool/overcast productive weather) ────────────
                    new Song(
                        "Deep Work", "Brain Waves", "Study Flow", "Lo-Fi", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                        "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500",
                        "Focused", "Medium"
                    ),
                    new Song(
                        "Code & Coffee", "Terminal Vibes", "Dark Mode", "Electronic", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                        "https://images.unsplash.com/photo-1484788984921-03950022c9ef?w=500",
                        "Focused", "Medium"
                    ),
                    new Song(
                        "Flow State", "Neural Net", "Productivity Mix", "Ambient", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                        "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500",
                        "Focused", "Low"
                    ),

                    // ── HAPPY mood (for sunny/warm spring weather) ────────────────────
                    new Song(
                        "Good Vibes Only", "Sunshine Band", "Bright Side", "Pop", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                        "https://images.unsplash.com/photo-1462275646964-a0e3386b89fa?w=500",
                        "Happy", "High"
                    ),
                    new Song(
                        "Dance in the Rain", "Confetti Drop", "Celebration", "Dance", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                        "https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=500",
                        "Happy", "High"
                    ),
                    new Song(
                        "Lemonade Stand", "The Juice Box", "Summer Crush", "Indie Pop", "English",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                        "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?w=500",
                        "Happy", "Medium"
                    ),

                    // ── NIGHT mood (for late night / low temp) ────────────────────────
                    new Song(
                        "Neon Lights", "Night Owl", "After Dark", "Electronic", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
                        "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=500",
                        "Night", "Medium"
                    ),
                    new Song(
                        "Midnight City", "City Pulse", "Urban Glow", "Synthwave", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
                        "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=500",
                        "Night", "Medium"
                    ),
                    new Song(
                        "3 AM Thoughts", "Insomniac", "Wide Awake", "Lo-Fi", "Instrumental",
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
                        "https://images.unsplash.com/photo-1509822929063-6b6723e8d5b4?w=500",
                        "Night", "Low"
                    )

                );

                songRepository.saveAll(initialSongs);
                System.out.println(">>> VibeSync: Seeded " + initialSongs.size() + " songs into MySQL! <<<");
            }

            List<Song> additionalSongs = Arrays.asList(
                new Song(
                    "Window Seat", "Paper Skies", "Passing Clouds", "Indie Folk", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                    "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=500",
                    "Calm", "Low"
                ),
                new Song(
                    "Open Road", "Daybreak Club", "Long Way Home", "Indie Pop", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                    "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500",
                    "Energetic", "High"
                ),
                new Song(
                    "Sunday Light", "Porchlight", "Slow Bloom", "Acoustic", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    "https://images.unsplash.com/photo-1500534623283-312aade485b7?w=500",
                    "Happy", "Medium"
                ),
                new Song(
                    "Raincheck", "June Avenue", "Soft Weather", "Alternative", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    "https://images.unsplash.com/photo-1515694346937-94d85e41e695?w=500",
                    "Cozy", "Low"
                ),
                new Song(
                    "Blue Hour", "Mira Sol", "Evening Notes", "Soul", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    "https://images.unsplash.com/photo-1497250681960-ef046c08a56e?w=500",
                    "Romantic", "Medium"
                ),
                new Song(
                    "Quiet Hours", "Northbound", "Soft Focus", "Lo-Fi", "Instrumental",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                    "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500",
                    "Focused", "Low"
                ),
                new Song(
                    "City After Dark", "Signal Night", "After Hours", "Electronic", "Instrumental",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                    "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=500",
                    "Night", "Medium"
                ),
                new Song(
                    "After the Rain", "June Avenue", "Soft Weather", "Indie", "English",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?w=500",
                    "Melancholic", "Low"
                ),
                new Song(
                    "Vennela Raagam (Telugu Preview)", "VibeSync Sessions", "Regional Previews", "Telugu Indie", "Telugu",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                    "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=500",
                    "Calm", "Low"
                ),
                new Song(
                    "Godavari Gaali (Telugu Preview)", "VibeSync Sessions", "Regional Previews", "Telugu Folk", "Telugu",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    "https://images.unsplash.com/photo-1500534623283-312aade485b7?w=500",
                    "Cozy", "Medium"
                ),
                new Song(
                    "Chinuku Chinuku (Telugu Preview)", "VibeSync Sessions", "Regional Previews", "Telugu Acoustic", "Telugu",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    "https://images.unsplash.com/photo-1515694346937-94d85e41e695?w=500",
                    "Happy", "Medium"
                ),
                new Song(
                    "Jathara Beat (Telugu Preview)", "VibeSync Sessions", "Regional Previews", "Telugu Folk", "Telugu",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                    "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
                    "Energetic", "High"
                ),
                new Song(
                    "Shaam Ka Safar (Hindi Preview)", "VibeSync Sessions", "Regional Previews", "Hindi Indie", "Hindi",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                    "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=500",
                    "Romantic", "Low"
                ),
                new Song(
                    "Barsaat Ki Dhun (Hindi Preview)", "VibeSync Sessions", "Regional Previews", "Hindi Acoustic", "Hindi",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    "https://images.unsplash.com/photo-1504701954957-2010ec3bcec1?w=500",
                    "Cozy", "Low"
                ),
                new Song(
                    "Rangon Ka Din (Hindi Preview)", "VibeSync Sessions", "Regional Previews", "Hindi Pop", "Hindi",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500",
                    "Happy", "High"
                ),
                new Song(
                    "Raat Ki Roshni (Hindi Preview)", "VibeSync Sessions", "Regional Previews", "Hindi Lo-Fi", "Hindi",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
                    "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=500",
                    "Night", "Medium"
                )
            );

            Set<String> existingTitles = songRepository.findAll().stream()
                    .map(song -> song.getTitle().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            List<Song> songsToAdd = additionalSongs.stream()
                    .filter(song -> !existingTitles.contains(song.getTitle().toLowerCase(Locale.ROOT)))
                    .toList();

            if (!songsToAdd.isEmpty()) {
                songRepository.saveAll(songsToAdd);
                System.out.println(">>> VibeSync: Added " + songsToAdd.size() + " new songs! <<<");
            }
        };
    }
}
