package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentInitiationResponseDto {
    private Long registrationId;
    private String preferenceId;
    private String paymentUrl;
    private BigDecimal totalAmount;
    private BigDecimal appFee;
    private BigDecimal organizerAmount;
    private String eventName;
    private String message;
}
