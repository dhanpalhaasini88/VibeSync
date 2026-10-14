package com.vibesync.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class WeatherService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${weather.api.key:}")
    private String apiKey;

    public JsonNode getWeather(String city) throws Exception {
        if (city == null || city.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a city name.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return getOpenMeteoWeather(city.trim());
        }
        return requestWeather("q=" + URLEncoder.encode(city.trim(), StandardCharsets.UTF_8));
    }

    public JsonNode getWeather(double latitude, double longitude) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            return getOpenMeteoWeather(latitude, longitude, "Your location", "");
        }
        return requestWeather("lat=" + latitude + "&lon=" + longitude);
    }

    private JsonNode getOpenMeteoWeather(String city) throws Exception {
        String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=1&language=en&format=json";
        JsonNode locations = requestOpenMeteoJson(url).path("results");
        if (!locations.isArray() || locations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching city was found.");
        }

        JsonNode location = locations.get(0);
        return getOpenMeteoWeather(
                location.path("latitude").asDouble(),
                location.path("longitude").asDouble(),
                location.path("name").asText(city),
                location.path("country_code").asText("")
        );
    }

    private JsonNode getOpenMeteoWeather(double latitude, double longitude, String city, String countryCode)
            throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code"
                + "&timezone=auto";
        JsonNode forecast = requestOpenMeteoJson(url);
        JsonNode current = forecast.path("current");
        if (current.isMissingNode() || current.isNull()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather data was unavailable.");
        }

        int code = current.path("weather_code").asInt(-1);
        boolean isDay = current.path("is_day").asInt(1) == 1;
        ObjectNode weather = objectMapper.createObjectNode();
        weather.put("name", city);
        weather.put("timezone", forecast.path("timezone").asText("UTC"));
        weather.put("time", current.path("time").asText(""));

        ObjectNode location = weather.putObject("sys");
        location.put("country", countryCode);

        ObjectNode measurements = weather.putObject("main");
        measurements.put("temp", current.path("temperature_2m").asDouble());
        measurements.put("feels_like", current.path("apparent_temperature").asDouble());
        measurements.put("humidity", current.path("relative_humidity_2m").asInt());

        ObjectNode condition = weather.putArray("weather").addObject();
        condition.put("main", getOpenMeteoCondition(code));
        condition.put("description", getOpenMeteoDescription(code));
        condition.put("icon", getOpenMeteoIcon(code, isDay));
        return weather;
    }

    private JsonNode requestOpenMeteoJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 429) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Weather rate limit reached. Try again shortly.");
        }
        if (response.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather provider is unavailable right now.");
        }
        return objectMapper.readTree(response.body());
    }

    private String getOpenMeteoCondition(int code) {
        if (code == 0 || code == 1) return "Clear";
        if (code == 2 || code == 3) return "Clouds";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if ((code >= 61 && code <= 67) || (code >= 80 && code <= 82)) return "Rain";
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "Snow";
        if (code >= 95 && code <= 99) return "Thunderstorm";
        return "Clouds";
    }

    private String getOpenMeteoDescription(int code) {
        return switch (code) {
            case 0 -> "clear sky";
            case 1 -> "mainly clear";
            case 2 -> "partly cloudy";
            case 3 -> "overcast";
            case 45, 48 -> "fog";
            case 51, 56 -> "light drizzle";
            case 53 -> "drizzle";
            case 55, 57 -> "dense drizzle";
            case 61, 66, 80 -> "light rain";
            case 63, 81 -> "rain showers";
            case 65, 67, 82 -> "heavy rain";
            case 71, 77, 85 -> "light snow";
            case 73 -> "snow";
            case 75, 86 -> "heavy snow";
            case 95 -> "thunderstorm";
            case 96 -> "thunderstorm with light hail";
            case 99 -> "thunderstorm with heavy hail";
            default -> "cloudy";
        };
    }

    private String getOpenMeteoIcon(int code, boolean isDay) {
        String suffix = isDay ? "d" : "n";
        if (code == 0) return "01" + suffix;
        if (code <= 2) return "02" + suffix;
        if (code == 3) return "04" + suffix;
        if (code == 45 || code == 48) return "50" + suffix;
        if (code >= 51 && code <= 57) return "09" + suffix;
        if ((code >= 61 && code <= 67) || (code >= 80 && code <= 82)) return "10" + suffix;
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "13" + suffix;
        if (code >= 95 && code <= 99) return "11" + suffix;
        return "02" + suffix;
    }

    private JsonNode requestWeather(String locationQuery) throws Exception {
        String url = "https://api.openweathermap.org/data/2.5/weather?"
                + locationQuery
                + "&appid=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&units=metric";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            if (response.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No matching city was found.");
            }
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "OpenWeather rejected the key. Check that WEATHER_API_KEY is valid and active."
                );
            }
            if (response.statusCode() == 429) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OpenWeather rate limit reached. Try again shortly.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenWeather is unavailable right now.");
        }

        JsonNode weather = objectMapper.readTree(response.body());
        long timestamp = weather.path("dt").asLong();
        int timezoneOffset = weather.path("timezone").asInt();
        if (timestamp > 0 && weather.isObject()) {
            ObjectNode weatherObject = (ObjectNode) weather;
            LocalDateTime localTime = Instant.ofEpochSecond(timestamp)
                    .atOffset(ZoneOffset.ofTotalSeconds(timezoneOffset))
                    .toLocalDateTime();
            weatherObject.put("time", localTime.toString());
            weatherObject.put("timezone", formatTimezone(timezoneOffset));
        }
        return weather;
    }

    private String formatTimezone(int offsetSeconds) {
        String offset = ZoneOffset.ofTotalSeconds(offsetSeconds).getId();
        return offset.equals("Z") ? "UTC" : "UTC" + offset;
    }
}