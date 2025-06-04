package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParticipantInfoDto {
    private Long dancerId;
    private String dancerName;
    private LocalDateTime registrationDate;
    private BigDecimal paidAmount;
}
