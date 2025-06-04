package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FinancialReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;

    private BigDecimal totalRevenue;
    private BigDecimal totalAppFees;
    private BigDecimal totalOrganizerAmount;

    private Map<String, Long> transactionsByPaymentMethod;
    private Map<EventType, BigDecimal> revenueByEventType;
}
