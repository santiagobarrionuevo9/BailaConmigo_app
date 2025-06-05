package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.EventType;

@Data
public class EventTypeSatisfactionDto {
    private EventType eventType;
    private String eventTypeName;
    private Double averageRating;
    private Integer totalRatedEvents;
    private Integer totalEvents;
}
