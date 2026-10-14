package com.vibesync.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.vibesync.entity.Song;
import com.vibesync.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "http://localhost:5173")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private SongService songService;

    // GET /api/weather?city=London → normalized weather data
    @GetMapping
    public JsonNode getWeather(@RequestParam String city) throws Exception {
        return weatherService.getWeather(city);
    }

    /**
        * GET /api/weather/vibe?city=Mumbai or /api/weather/vibe?lat=...&lon=...
     *
     * Returns everything the Weather Vibe page needs in one call:
     * {
     *   "city": "Mumbai",
     *   "temperature": 31.5,
     *   "feelsLike": 35.2,
     *   "condition": "Clear",
     *   "description": "clear sky",
     *   "icon": "01d",
     *   "humidity": 70,
     *   "mood": "Energetic",
     *   "moodDescription": "Hot sunny day — turn up the energy! ☀️🔥",
     *   "songs": [ ... list of matching songs ... ]
     * }
     */
    @GetMapping("/vibe")
    public Map<String, Object> getWeatherVibe(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) throws Exception {

        // 1. Fetch current weather from OpenWeather
        JsonNode weatherData;
        if (lat != null && lon != null) {
            weatherData = weatherService.getWeather(lat, lon);
        } else if (city != null && !city.isBlank()) {
            weatherData = weatherService.getWeather(city.trim());
        } else {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Provide a city or both latitude and longitude."
                );
        }

        // 2. Extract the fields we need
        double temperature  = weatherData.path("main").path("temp").asDouble();
        double feelsLike    = weatherData.path("main").path("feels_like").asDouble();
        int humidity        = weatherData.path("main").path("humidity").asInt();
        String condition    = weatherData.path("weather").get(0).path("main").asText();
        String description  = weatherData.path("weather").get(0).path("description").asText();
        String icon         = weatherData.path("weather").get(0).path("icon").asText();
        String cityName     = weatherData.path("name").asText();
        String countryCode  = weatherData.path("sys").path("country").asText();

        String observedAt = weatherData.path("time").asText("");

        // 4. Map weather → mood using our WeatherVibeMapper without time-based overrides.
        String mood = WeatherVibeMapper.getMood(condition, temperature);
        String moodDescription = WeatherVibeMapper.getMoodDescription(mood, condition, temperature);

        // 5. Fetch songs that match this mood from our database
        List<Song> matchingSongs = songService.getSongsByMood(mood);

        // 6. Build the response
        Map<String, Object> response = new HashMap<>();
        response.put("city",            countryCode.isBlank() ? cityName : cityName + ", " + countryCode);
        response.put("observedAt",      observedAt);
        response.put("timezone",        weatherData.path("timezone").asText("UTC"));
        response.put("temperature",     Math.round(temperature * 10.0) / 10.0);
        response.put("feelsLike",       Math.round(feelsLike * 10.0) / 10.0);
        response.put("condition",       condition);
        response.put("description",     description);
        response.put("icon",            icon);
        response.put("humidity",        humidity);
        response.put("mood",            mood);
        response.put("moodDescription", moodDescription);
        response.put("songs",           matchingSongs);

        return response;
    }
}