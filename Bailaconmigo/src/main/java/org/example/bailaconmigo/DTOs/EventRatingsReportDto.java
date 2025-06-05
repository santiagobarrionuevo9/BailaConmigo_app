package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EventRatingsReportDto {
    private LocalDateTime generatedAt;
    private Integer totalEventRatings;
    private Double averageEventRating;
    private Map<Integer, Long> starsDistribution;
    private Double commentPercentage;
    private List<TopRatedEventDto> topRatedEvents;
    private List<TopRatedEventDto> mostRatedEvents;
}