package org.example.bailaconmigo.DTOs;

import lombok.Data;

@Data
public class OrganizerRatingReportDto {
    private Long organizerId;
    private String organizerName;
    private Double averageRating;
    private Integer totalRatings;
    private Integer totalEvents;
    private Integer ratedEvents;
}
