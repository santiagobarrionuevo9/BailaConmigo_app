package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpcomingEventReportDto {
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private String organizerName;
    private String cityName;

    private Integer capacity;
    private int currentRegistrations;
    private int availableSpots;
    private double occupancyRate;
}
