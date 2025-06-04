package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationEventReportDto {
    private String countryName;
    private String cityName;

    private int totalEvents;
    private int activeEvents;
    private int totalRegistrations;

    private BigDecimal totalRevenue;
}
