package org.example.bailaconmigo.Services;

import jakarta.transaction.Transactional;
import org.example.bailaconmigo.DTOs.CancelRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationResponseDto;
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

    /**
     * Registra un bailarín en un evento
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

        // Crear la inscripción
        EventRegistration registration = new EventRegistration();
        registration.setDancer(dancer);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CONFIRMADO);

        // Guardar la inscripción
        EventRegistration savedRegistration = registrationRepository.save(registration);

        // Enviar notificaciones
        notificationService.sendRegistrationConfirmationToDancer(savedRegistration);
        notificationService.sendRegistrationNotificationToOrganizer(savedRegistration);

        return convertToDto(savedRegistration);
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

        // Cancelar inscripción
        registration.setStatus(RegistrationStatus.CANCELADO);
        registration.setCancelReason(dto.getCancelReason());
        registrationRepository.save(registration);

        // No se envía notificación de cancelación de inscripción, pero podría implementarse
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
