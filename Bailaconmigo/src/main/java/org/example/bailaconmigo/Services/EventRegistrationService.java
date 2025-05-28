package org.example.bailaconmigo.Services;

import jakarta.transaction.Transactional;
import org.example.bailaconmigo.DTOs.CancelRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationResponseDto;
import org.example.bailaconmigo.DTOs.PaymentInitiationResponseDto;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;
import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.EventRegistration;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.EventRegistrationRepository;
import org.example.bailaconmigo.Repositories.EventRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventRegistrationService {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService notificationService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    /**
     * Registra un bailarín en un evento con cobro mediante Mercado Pago
     */
    @Transactional
    public PaymentInitiationResponseDto registerDancerForEventWithPayment(Long dancerId, EventRegistrationRequestDto dto) {
        if (dancerId == null || dto.getEventId() == null) {
            throw new RuntimeException("IDs inválidos para la inscripción.");
        }

        User dancer = userRepository.findById(dancerId)
                .orElseThrow(() -> new RuntimeException("Bailarín no encontrado"));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Validaciones
        if (event.getStatus() != EventStatus.ACTIVO) {
            throw new RuntimeException("No se puede inscribir a un evento que no está activo.");
        }

        if (!event.hasAvailableCapacity()) {
            throw new RuntimeException("El evento no tiene cupo disponible.");
        }

        if (registrationRepository.existsByEventAndDancer(event, dancer)) {
            throw new RuntimeException("Ya estás inscrito en este evento.");
        }

        if (event.getPrice() == null || event.getPrice() <= 0) {
            throw new RuntimeException("El evento debe tener un precio válido.");
        }

        // Verificar que el organizador tenga token de Mercado Pago
        if (event.getOrganizer().getUser().getMercadoPagoToken() == null ||
                event.getOrganizer().getUser().getMercadoPagoToken().isEmpty()) {
            throw new RuntimeException("El organizador no tiene configurado su medio de pago.");
        }

        // Calcular montos
        BigDecimal eventPrice = BigDecimal.valueOf(event.getPrice());
        BigDecimal appFee = mercadoPagoService.calculateAppFee(event.getPrice());
        BigDecimal organizerAmount = mercadoPagoService.calculateOrganizerAmount(event.getPrice());

        // Crear la inscripción con estado PENDIENTE_PAGO
        EventRegistration registration = new EventRegistration();
        registration.setDancer(dancer);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDIENTE);
        registration.setPaidAmount(eventPrice);
        registration.setAppFee(appFee);
        registration.setOrganizerAmount(organizerAmount);
        registration.setPaymentStatus("pending");

        // Guardar la inscripción temporal
        EventRegistration savedRegistration = registrationRepository.save(registration);

        try {
            // Crear preferencia de pago en Mercado Pago
            String preferenceId = mercadoPagoService.createSplitPaymentPreference(dancer, event, savedRegistration.getId());

            // Actualizar la inscripción con el ID de preferencia
            savedRegistration.setPaymentPreferenceId(preferenceId);
            savedRegistration.setPaymentReference("REG_" + savedRegistration.getId() + "_EVENT_" + event.getId());
            registrationRepository.save(savedRegistration);

            // Crear respuesta con URL de pago
            PaymentInitiationResponseDto response = new PaymentInitiationResponseDto();
            response.setRegistrationId(savedRegistration.getId());
            response.setPreferenceId(preferenceId);
            response.setPaymentUrl(preferenceId);
            response.setTotalAmount(eventPrice);
            response.setAppFee(appFee);
            response.setOrganizerAmount(organizerAmount);
            response.setEventName(event.getName());
            response.setMessage("Redirigiendo a Mercado Pago para completar el pago...");

            return response;

        } catch (Exception e) {
            // Si falla la creación del pago, eliminar la inscripción temporal
            registrationRepository.delete(savedRegistration);
            throw new RuntimeException("Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Método original mantenido para eventos gratuitos
     */
    @Transactional
    public EventRegistrationResponseDto registerDancerForEvent(Long dancerId, EventRegistrationRequestDto dto) {
        if (dancerId == null || dto.getEventId() == null) {
            throw new RuntimeException("IDs inválidos para la inscripción.");
        }

        User dancer = userRepository.findById(dancerId)
                .orElseThrow(() -> new RuntimeException("Bailarín no encontrado"));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Validaciones
        if (event.getStatus() != EventStatus.ACTIVO) {
            throw new RuntimeException("No se puede inscribir a un evento que no está activo.");
        }

        if (!event.hasAvailableCapacity()) {
            throw new RuntimeException("El evento no tiene cupo disponible.");
        }

        if (registrationRepository.existsByEventAndDancer(event, dancer)) {
            throw new RuntimeException("Ya estás inscrito en este evento.");
        }

        // Para eventos gratuitos o sin precio
        if (event.getPrice() == null || event.getPrice() <= 0) {
            EventRegistration registration = new EventRegistration();
            registration.setDancer(dancer);
            registration.setEvent(event);
            registration.setRegistrationDate(LocalDateTime.now());
            registration.setStatus(RegistrationStatus.CONFIRMADO);
            registration.setPaidAmount(BigDecimal.ZERO);

            EventRegistration savedRegistration = registrationRepository.save(registration);

            // Enviar notificaciones
            notificationService.sendRegistrationConfirmationToDancer(savedRegistration);
            notificationService.sendRegistrationNotificationToOrganizer(savedRegistration);

            return convertToDto(savedRegistration);
        } else {
            throw new RuntimeException("Este evento requiere pago. Use el endpoint de pago correspondiente.");
        }
    }

    /**
     * Confirma el pago y actualiza el estado de la inscripción
     */
    @Transactional
    public void confirmPayment(Long registrationId, String paymentId, String paymentStatus,
                               String paymentMethod, String paymentDetails) {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        registration.setPaymentId(paymentId);
        registration.setPaymentStatus(paymentStatus);
        registration.setPaymentMethod(paymentMethod);
        registration.setPaymentDetails(paymentDetails);
        registration.setPaymentDate(LocalDateTime.now());

        if ("approved".equals(paymentStatus)) {
            registration.setStatus(RegistrationStatus.CONFIRMADO);

            // Enviar notificaciones
            notificationService.sendRegistrationConfirmationToDancer(registration);
            notificationService.sendRegistrationNotificationToOrganizer(registration);

        } else if ("rejected".equals(paymentStatus)) {
            registration.setStatus(RegistrationStatus.CANCELADO);
            registration.setCancelReason("Pago rechazado");
        }

        registrationRepository.save(registration);
    }

    /**
     * Cancela la inscripción de un bailarín a un evento
     */
    @Transactional
    public void cancelRegistration(Long dancerId, Long eventId, CancelRegistrationRequestDto dto) {
        User dancer = userRepository.findById(dancerId)
                .orElseThrow(() -> new RuntimeException("Bailarín no encontrado"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        EventRegistration registration = registrationRepository.findByEventAndDancer(event, dancer)
                .orElseThrow(() -> new RuntimeException("No se encontró inscripción para cancelar"));

        // Validar si ya está cancelada
        if (registration.getStatus() == RegistrationStatus.CANCELADO) {
            throw new RuntimeException("La inscripción ya fue cancelada anteriormente");
        }

        // Reembolso si aplica
        if (RegistrationStatus.CONFIRMADO.equals(registration.getStatus()) &&
                "approved".equalsIgnoreCase(registration.getPaymentStatus()) &&
                registration.getPaymentId() != null) {
            try {
                mercadoPagoService.refundPayment(registration.getPaymentId(), registration.getEvent().getOrganizer().getUser().getMercadoPagoToken());
                registration.setPaymentStatus("refunded");
                registration.setCancelReason("Inscripción cancelada - Reembolso automático");
            } catch (Exception e) {
                registration.setCancelReason("Inscripción cancelada - Error en reembolso: " + e.getMessage());
                System.err.println("Error al reembolsar pago " + registration.getPaymentId() + ": " + e.getMessage());
            }
        } else {
            registration.setCancelReason(dto.getCancelReason() != null ? dto.getCancelReason() : "Inscripción cancelada");
        }

        // Cancelar inscripción
        registration.setStatus(RegistrationStatus.CANCELADO);
        registrationRepository.save(registration);

        // Notificar al usuario (opcional)
        notificationService.sendCancelationPackage(registration);
    }

    /**
     * Obtiene todas las inscripciones de un bailarín
     */
    public List<EventRegistrationResponseDto> getRegistrationsByDancer(Long dancerId) {
        User dancer = userRepository.findById(dancerId)
                .orElseThrow(() -> new RuntimeException("Bailarín no encontrado"));

        return registrationRepository.findByDancer(dancer).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las inscripciones para un evento
     */
    public List<EventRegistrationResponseDto> getRegistrationsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        return registrationRepository.findByEvent(event).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una inscripción a su DTO correspondiente
     */
    private EventRegistrationResponseDto convertToDto(EventRegistration registration) {
        EventRegistrationResponseDto dto = new EventRegistrationResponseDto();
        dto.setId(registration.getId());
        dto.setEventId(registration.getEvent().getId());
        dto.setEventName(registration.getEvent().getName());
        dto.setEventDateTime(registration.getEvent().getDateTime());
        dto.setDancerId(registration.getDancer().getId());
        dto.setDancerName(registration.getDancer().getFullName());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus());
        return dto;
    }
}
