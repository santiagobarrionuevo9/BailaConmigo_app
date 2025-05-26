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
    private String lastPaymentReference;
    // 🔐 Token de acceso de Mercado Pago
    private String mercadoPagoToken;
    // Relación con City
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    // Relación con Country
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    private LocalDate subscriptionExpiration;

    // Campos para recuperación de contraseña
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    // Métodos helper para obtener nombres (backward compatibility)
    public String getCityName() {
        return city != null ? city.getName() : null;
    }

    public String getCountryName() {
        return country != null ? country.getName() : null;
    }

    // Métodos helper para establecer nombres (si necesitas mantener compatibilidad con código existente)
    @Deprecated
    public void setCityName(String cityName) {
        // Este método se mantiene para compatibilidad pero se recomienda usar setCity()
        // La lógica real debe manejarse en el servicio buscando la City por nombre
    }

    @Deprecated
    public void setCountryName(String countryName) {
        // Este método se mantiene para compatibilidad pero se recomienda usar setCountry()
        // La lógica real debe manejarse en el servicio buscando el Country por nombre
    }
}


