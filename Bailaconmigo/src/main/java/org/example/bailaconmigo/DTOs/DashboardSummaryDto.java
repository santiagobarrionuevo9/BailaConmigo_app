package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class DashboardSummaryDto {
    private int totalEvents;
    private int totalRegistrations;
    private BigDecimal totalRevenue;
    private List<UpcomingEventReportDto> upcomingEvents;

    // Getters y setters
}

