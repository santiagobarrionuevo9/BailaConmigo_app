package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;

@Data
public class DanceStyleSatisfactionDto {
    private DanceStyle danceStyle;
    private String styleName;
    private Double averageRating;
    private Integer totalRatedEvents;
}
