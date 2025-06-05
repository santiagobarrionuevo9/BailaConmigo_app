package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProfileRatingsReportDto {
    private LocalDateTime generatedAt;
    private Integer totalProfileRatings;
    private Double averageProfileRating;
    private Map<Integer, Long> starsDistribution;
    private Double commentPercentage;
    private List<TopRatedProfileDto> topRatedProfiles;
    private List<TopRatedProfileDto> mostRatedProfiles;
}
