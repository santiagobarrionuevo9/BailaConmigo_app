package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String fullName;
    private String role;
    private String subscriptionType;
    private LocalDate subscriptionExpiration;
    private String email;
}
