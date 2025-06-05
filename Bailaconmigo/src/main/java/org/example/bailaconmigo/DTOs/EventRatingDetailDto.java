package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EventRatingDetailDto {
    private Long eventId;
    private String eventName;
    private EventType eventType;
    private LocalDateTime eventDate;
    private Integer totalRatings;
    private Double averageRating;
    private Map<Integer, Long> starsDistribution;
    private List<RatingCommentDto> recentComments;
}