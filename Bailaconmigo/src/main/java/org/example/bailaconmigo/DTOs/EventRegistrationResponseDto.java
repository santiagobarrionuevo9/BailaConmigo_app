package org.example.bailaconmigo.DTOs;

import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;

import java.math.BigDecimal;
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

    // ===== CAMPOS DE PAGO =====
    private BigDecimal paidAmount;
    private BigDecimal appFee;
    private BigDecimal organizerAmount;
    private String paymentPreferenceId;
    private String paymentId;
    private String paymentStatus;
    private String paymentReference;
    private LocalDateTime paymentDate;
    private String paymentMethod;

    // URL de pago para redirigir al usuario
    private String paymentUrl;
}
