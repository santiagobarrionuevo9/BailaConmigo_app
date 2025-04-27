package org.example.bailaconmigo.DTOs;

import lombok.Data;

@Data
public class EventRatingRequestDto {
    private Long eventId;
    private Integer stars;
    private String comment;
}
