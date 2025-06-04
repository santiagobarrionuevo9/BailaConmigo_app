package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizerEventReportDto {
    private Long organizerId;
    private String organizerName;

    private int totalEvents;
    private int activeEvents;
    private int cancelledEvents;
    private int totalRegistrations;

    private BigDecimal totalRevenue;
    private double averageRating;
}
