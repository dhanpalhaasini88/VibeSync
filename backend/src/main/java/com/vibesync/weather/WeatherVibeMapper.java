package com.vibesync.weather;

/**
 * WeatherVibeMapper — the brain of the Weather Vibe feature.
 *
 * Takes two inputs:
 *   1. weatherCondition  — from OpenWeather (e.g. "Rain", "Clear", "Clouds")
 *   2. temperature       — in Celsius (e.g. 15.5)
 *
 * Returns one of these moods:
 *   Calm | Energetic | Cozy | Melancholic | Romantic | Focused | Happy
 *
 * These mood strings EXACTLY match the mood field in the songs table.
 */
public class WeatherVibeMapper {

    /**
     * Map weather data to a music mood using weather and temperature only.
     *
     * Logic:
     *   - Stormy/thunderstorm → Melancholic (heavy, dramatic)
     *   - Rainy + cold        → Melancholic
     *   - Rainy + warm        → Cozy
     *   - Snowy               → Cozy
     *   - Clear + hot (>28°C) → Energetic
     *   - Clear + warm        → Happy
     *   - Clear + mild        → Calm
     *   - Cloudy + cool       → Focused
     *   - Cloudy + warm       → Cozy
     *   - Default             → Calm
     */
    public static String getMood(String weatherCondition, double temperature) {

        String condition = weatherCondition.toLowerCase();

        // ── Stormy / Thunderstorm ────────────────────────────────────────────
        if (condition.contains("thunderstorm") || condition.contains("storm")) {
            return "Melancholic";
        }

        // ── Rainy ────────────────────────────────────────────────────────────
        if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("shower")) {
            return temperature < 15 ? "Melancholic" : "Cozy";
        }

        // ── Snowy / Mist / Fog ───────────────────────────────────────────────
        if (condition.contains("snow") || condition.contains("sleet") ||
            condition.contains("mist") || condition.contains("fog") || condition.contains("haze")) {
            return "Cozy";
        }

        // ── Clear sky ────────────────────────────────────────────────────────
        if (condition.contains("clear")) {
            if (temperature >= 28) return "Energetic";      // hot sunny day
            if (temperature >= 20) return "Happy";           // warm pleasant day
            if (temperature >= 12) return "Calm";            // mild sunny day
            return "Cozy";                                   // cold clear day
        }

        // ── Cloudy ───────────────────────────────────────────────────────────
        if (condition.contains("cloud") || condition.contains("overcast")) {
            return temperature < 18 ? "Focused" : "Cozy";
        }

        // ── Mild and pleasant default for neutral conditions ─────────────────
        return "Calm";
    }

    /**
     * Get a human-readable description of why this mood was chosen.
     * Shown on the Weather Vibe page so the user understands the recommendation.
     */
    public static String getMoodDescription(String mood, String weatherCondition, double temperature) {
        return switch (mood) {
            case "Melancholic" -> "Stormy or cold and rainy outside — let the music match your mood 🌧️";
            case "Cozy"        -> "Cloudy and warm — perfect for cozy vibes indoors ☕";
            case "Energetic"   -> "Hot sunny day — turn up the energy! ☀️🔥";
            case "Happy"       -> "Beautiful warm weather — time for happy tunes! 😊";
            case "Calm"        -> "Mild and peaceful outside — calm music to match 🍃";
            case "Focused"     -> "Cool overcast day — perfect for deep focus 🎯";
            case "Romantic"    -> "Soft weather and a relaxed mood — romantic tracks for the moment 🌆";
            default            -> "Music picked just for you based on the weather 🎵";
        };
    }
}
