package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event_registrations")
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User dancer;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    private String cancelReason;

    // Podría añadirse información adicional como:
    // - Precio pagado
    // - Método de pago
    // - Referencia de pago
    // - Etc.
}
