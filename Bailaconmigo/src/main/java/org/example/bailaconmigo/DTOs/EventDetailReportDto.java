package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDetailReportDto {
    private Long eventId;
    private String eventName;
    private EventType eventType;
    private EventStatus eventStatus;
    private LocalDateTime eventDate;
    private String organizerName;
    private String cityName;
    private String countryName;

    // Capacidad y ocupación
    private Integer capacity;
    private int totalRegistrations;
    private int confirmedRegistrations;
    private int pendingRegistrations;
    private int cancelledRegistrations;
    private int availableCapacity;
    private double occupancyRate;

    // Información del evento
    private Double price;
    private List<DanceStyle> danceStyles;

    // Métricas financieras
    private BigDecimal totalRevenue;

    // Calificaciones
    private double averageRating;
    private int totalRatings;

    // Participantes
    private List<ParticipantInfoDto> participants;
}

