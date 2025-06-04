package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class EventGeneralReportDto {
    private LocalDateTime generatedAt;

    // Estadísticas generales de eventos
    private int totalEvents;
    private int activeEvents;
    private int cancelledEvents;

    // Estadísticas de inscripciones
    private int totalRegistrations;
    private int confirmedRegistrations;
    private int pendingRegistrations;
    private int cancelledRegistrations;

    // Métricas financieras
    private BigDecimal totalRevenue;
    private double averageRegistrationsPerEvent;

    // Distribuciones
    private Map<EventType, Long> eventsByType;
    private Map<DanceStyle, Long> danceStylePopularity;
}
