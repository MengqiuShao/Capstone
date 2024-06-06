package com.weatherhome.homepage.controller;

import com.weatherhome.homepage.service.RecommendationService;
import com.weatherhome.homepage.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/recommendation")
    public Map<String, Object> getRecommendation(@RequestParam String city) {
        Map<String, String> weatherData = weatherService.getWeather(city);
        return recommendationService.generateRecommendation(weatherData);
    }
}
