package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;

import java.time.LocalDateTime;

@Data
public class EventRegistrationResponseDto {
    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDateTime;
    private Long dancerId;
    private String dancerName;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
}
