package org.example.bailaconmigo.DTOs;


import lombok.Data;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;

import java.time.LocalDate;



@Data
public class RegisterRequestDto {
    private String fullName;
    private String email;
    private String password;
    private String gender;
    private LocalDate birthdate;
    private Long cityId;        // En lugar de String city
    private Long countryId;     // Nuevo campo

    private Role role;
    private SubscriptionType subscriptionType;
}


