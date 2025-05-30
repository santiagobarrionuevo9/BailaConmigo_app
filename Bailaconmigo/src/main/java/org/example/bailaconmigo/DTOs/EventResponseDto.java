package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Data
public class EventResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private String address;
    private Integer capacity;
    private Double price;
    private Set<DanceStyle> danceStyles;
    private String additionalInfo;
    private List<String> mediaUrls;
    private String organizerName;
    private Long organizerId;
    private Double averageRating;
    private EventType eventType;
    private EventStatus status;
    private String cancellationReason;
    private int availableCapacity;
}
