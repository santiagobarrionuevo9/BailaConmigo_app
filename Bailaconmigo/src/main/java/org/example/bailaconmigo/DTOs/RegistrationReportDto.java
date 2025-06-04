package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RegistrationReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;

    private int totalRegistrations;
    private int confirmedRegistrations;
    private int pendingRegistrations;
    private int cancelledRegistrations;

    private BigDecimal periodRevenue;
    private Map<LocalDate, Long> registrationsByDay;
}
