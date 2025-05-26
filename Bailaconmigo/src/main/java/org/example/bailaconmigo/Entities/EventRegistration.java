package org.example.bailaconmigo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;

import java.math.BigDecimal;
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

    // ===== CAMPOS DE PAGO =====

    // Precio pagado por la inscripción
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    // Fee que se queda la app (10%)
    @Column(name = "app_fee", precision = 10, scale = 2)
    private BigDecimal appFee;

    // Monto que va al organizador (90%)
    @Column(name = "organizer_amount", precision = 10, scale = 2)
    private BigDecimal organizerAmount;

    // ID de la preferencia de Mercado Pago
    @Column(name = "payment_preference_id")
    private String paymentPreferenceId;

    // ID del pago en Mercado Pago
    @Column(name = "payment_id")
    private String paymentId;

    // Estado del pago
    @Column(name = "payment_status")
    private String paymentStatus;

    // Referencia externa del pago
    @Column(name = "payment_reference")
    private String paymentReference;

    // Fecha del pago
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Método de pago usado
    @Column(name = "payment_method")
    private String paymentMethod;

    // JSON con detalles adicionales del pago
    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails;
}
