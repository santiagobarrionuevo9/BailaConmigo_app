package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.EventType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopRatedEventDto {
    private Long eventId;
    private String eventName;
    private EventType eventType;
    private Double averageRating;
    private Integer totalRatings;
}
