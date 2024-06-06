package com.weatherhome.homepage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    public Map<String, String> getWeather(String city) {
        String url = getWeatherRequest(city);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> weatherData = new LinkedHashMap<>();  // 使用LinkedHashMap来保证顺序
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode mainNode = root.path("main");
            JsonNode weatherNode = root.path("weather").get(0);
            JsonNode sysNode = root.path("sys");

            weatherData.put("city", city);
            weatherData.put("country", sysNode.path("country").asText());
            weatherData.put("temperature", String.valueOf(mainNode.path("temp").asInt()));
            weatherData.put("feels_like", String.valueOf(mainNode.path("feels_like").asInt()));
            weatherData.put("humidity", String.valueOf(mainNode.path("humidity").asInt()));
            weatherData.put("wind_speed", String.valueOf(root.path("wind").path("speed").asInt()));
            weatherData.put("weather_condition", weatherNode.path("main").asText().toLowerCase());
            weatherData.put("weather_description", weatherNode.path("description").asText().toLowerCase());
            weatherData.put("max_temp", String.valueOf(mainNode.path("temp_max").asInt()) + "°C");
            weatherData.put("min_temp", String.valueOf(mainNode.path("temp_min").asInt()) + "°C");
            weatherData.put("sunrise", String.valueOf(sysNode.path("sunrise").asLong()));
            weatherData.put("sunset", String.valueOf(sysNode.path("sunset").asLong()));


            String weatherType = generateWeatherType(weatherData);
            weatherData.put("weather_type", weatherType);
            logger.info("Generated weatherType: {}", weatherType);

            String recommendationType = generateRecommendationType(weatherType);
            weatherData.put("recommendation_type", recommendationType);
            logger.info("Generated recommendationType: {}", recommendationType);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    private String getWeatherRequest(String city) {
        if (apiUrl == null || apiKey == null) {
            throw new IllegalStateException("API URL or API Key is not configured properly.");
        }

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?q=").append(URLEncoder.encode(city, StandardCharsets.UTF_8));
        urlBuilder.append("&appid=").append(apiKey);
        urlBuilder.append("&units=metric");
        return urlBuilder.toString();
    }

    private String generateWeatherType(Map<String, String> weatherData) {
        String weatherDescription = weatherData.get("weather_description");
        logger.info("Weather description: {}", weatherDescription);

        if (weatherDescription.contains("clear")) {
            logger.info("Matched weather description to clear_sky");
            return "clear_sky";
        } else if (weatherDescription.contains("few clouds")) {
            logger.info("Matched weather description to few_clouds");
            return "few_clouds";
        } else if (weatherDescription.contains("scattered clouds")) {
            logger.info("Matched weather description to scattered_clouds");
            return "scattered_clouds";
        } else if (weatherDescription.contains("broken clouds")) {
            logger.info("Matched weather description to broken_clouds");
            return "broken_clouds";
        } else if (weatherDescription.contains("overcast clouds")) {
            logger.info("Matched weather description to overcast_clouds");
            return "broken_clouds"; // overcast clouds 也可以被认为是 broken clouds
        } else if (weatherDescription.contains("shower rain")) {
            logger.info("Matched weather description to shower_rain");
            return "shower_rain";
        } else if (weatherDescription.contains("rain")) {
            logger.info("Matched weather description to rain");
            return "rain";
        } else if (weatherDescription.contains("thunderstorm")) {
            logger.info("Matched weather description to thunderstorm");
            return "thunderstorm";
        } else if (weatherDescription.contains("snow")) {
            logger.info("Matched weather description to snow");
            return "snow";
        } else if (weatherDescription.contains("mist")) {
            logger.info("Matched weather description to mist");
            return "mist";
        }

        logger.info("No match found for weather description, returning other");
        return "other";
    }

    private String generateRecommendationType(String weatherType) {
        switch (weatherType) {
            case "clear_sky":
                return "highly_recommended";
            case "few_clouds":
                return "recommended";
            case "scattered_clouds":
            case "broken_clouds":
                return "recommended";
            case "shower_rain":
            case "mist":
                return "low_recommended";
            case "rain":
            case "thunderstorm":
            case "snow":
                return "not_recommended";
            default:
                return "other";
        }
    }
}
