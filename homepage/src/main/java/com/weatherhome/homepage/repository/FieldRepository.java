package com.weatherhome.homepage.repository;

import com.weatherhome.homepage.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findTop5ByRecommendationTypeOrderByRatingDesc(String recommendationType);
}

