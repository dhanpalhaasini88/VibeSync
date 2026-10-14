package com.vibesync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vibesync.entity.Song;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiRecommendationService {

        private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public GeminiRecommendationService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model:gemini-3.8-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> suggestSong(String mood, List<Song> songs) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI suggestions are not set up yet. Add GEMINI_API_KEY to the backend environment."
            );
        }
        if (songs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There are no songs in the catalog yet.");
        }

        String catalog = songs.stream()
                .map(song -> String.format("- %s | %s | mood: %s | genre: %s",
                        song.getTitle(), song.getArtist(), song.getMood(), song.getGenre()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse("");
        String prompt = "Choose the single best track for the requested mood from this catalog. "
                + "Only choose a title exactly as written. Return JSON with keys songTitle and reason; "
                + "reason must be one short sentence. Requested mood: " + mood
                + "\nCatalog:\n" + catalog;

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        contents.addObject().putArray("parts").addObject().put("text", prompt);
        requestBody.putObject("generationConfig").put("responseMimeType", "application/json");

        String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
        URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                + encodedModel + ":generateContent");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            HttpStatus status = response.statusCode() == 429
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.BAD_GATEWAY;
            throw new ResponseStatusException(status, "Gemini could not create a suggestion right now.");
        }

        JsonNode responseBody = objectMapper.readTree(response.body());
        String generatedJson = responseBody.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text").asText("");
        if (generatedJson.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty suggestion.");
        }

        JsonNode suggestion;
        try {
            suggestion = objectMapper.readTree(generatedJson);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an invalid suggestion.");
        }

        String title = suggestion.path("songTitle").asText("").trim();
        Song selectedSong = songs.stream()
                .filter(song -> song.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini did not select a song from the catalog. Please try again."
                ));
        String reason = suggestion.path("reason").asText("A match for your selected mood.").trim();

        return Map.of("song", selectedSong, "reason", reason);
    }
}