package org.example.bailaconmigo.DTOs;

import lombok.Data;

@Data
public class RatingRequestDto {
    private Long profileId;
    private Integer stars;
    private String comment;
}