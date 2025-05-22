package org.example.bailaconmigo.Entities;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String password;
    private String gender;
    private LocalDate birthdate;
    private String city;
    private String lastPaymentReference;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    private LocalDate subscriptionExpiration;

    // 🔐 Token de acceso de Mercado Pago
    private String mercadoPagoToken;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;
}


