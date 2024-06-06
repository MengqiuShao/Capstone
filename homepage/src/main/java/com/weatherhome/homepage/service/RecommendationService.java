package com.weatherhome.homepage.service;

import com.weatherhome.homepage.model.Field;
import com.weatherhome.homepage.model.Recommendation;
import com.weatherhome.homepage.repository.FieldRepository;
import com.weatherhome.homepage.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private FieldRepository fieldRepository;

    public Map<String, Object> generateRecommendation(Map<String, String> weatherData) {
        Map<String, Object> recommendation = new LinkedHashMap<>();
        String weatherType = weatherData.get("weather_type");


        System.out.println("Weather Type: " + weatherType);

        Optional<Recommendation> recommendationEntityOptional = recommendationRepository.findTopByTypeOrderByScoreDesc(weatherType);


        if (recommendationEntityOptional.isPresent()) {
            Recommendation recommendationEntity = recommendationEntityOptional.get();
            recommendation.put("advice", recommendationEntity.getMessage());
            System.out.println("Recommendation found: " + recommendationEntity.getMessage());
        } else {
            recommendation.put("advice", "No recommendation available for the current weather condition.");
            System.out.println("No recommendation found for weather type: " + weatherType);
        }

        String recommendationType = determineRecommendationType(weatherType);
        boolean showHikingList = !recommendationType.equals("not_recommend");
        recommendation.put("showHikingList", showHikingList);

        if (showHikingList) {
            List<Field> fields = fieldRepository.findTop5ByRecommendationTypeOrderByRatingDesc(recommendationType);
            System.out.println("Fields found: " + fields.size());
            List<Map<String, Object>> fieldData = new ArrayList<>();
            for (Field field : fields) {
                Map<String, Object> fieldInfo = new HashMap<>();
                fieldInfo.put("name", field.getName());
                fieldInfo.put("rating", field.getRating());
                fieldInfo.put("recommendationType", field.getRecommendationType());
                fieldInfo.put("location", field.getLocation());
                fieldInfo.put("difficulty", field.getDifficulty());
                fieldInfo.put("distance", field.getDistance());
                fieldInfo.put("estimatedTime", field.getEstimatedTime());
                fieldInfo.put("imageUrl", field.getImageUrl());
                fieldData.add(fieldInfo);
            }
            recommendation.put("fields", fieldData);
        }

        recommendation.putAll(weatherData);
        return recommendation;
    }

    private String determineRecommendationType(String weatherType) {
        switch (weatherType) {
            case "clear_sky":
            case "few_clouds":
                return "highly_recommend";
            case "scattered_clouds":
            case "broken_clouds":
                return "recommended";
            case "shower_rain":
            case "mist":
                return "low_recommend";
            case "rain":
            case "thunderstorm":
            case "snow":
                return "not_recommend";
            default:
                return "not_recommend";
        }
    }
}
