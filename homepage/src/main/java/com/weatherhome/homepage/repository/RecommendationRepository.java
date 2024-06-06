package com.weatherhome.homepage.repository;

import com.weatherhome.homepage.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    Optional<Recommendation> findTopByTypeOrderByScoreDesc(String type);
}
