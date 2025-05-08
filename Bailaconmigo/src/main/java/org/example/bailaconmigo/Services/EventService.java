package org.example.bailaconmigo.Services;

import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.*;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Repositories.EventRatingRepository;
import org.example.bailaconmigo.Repositories.EventRepository;
import org.example.bailaconmigo.Repositories.OrganizerProfileRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRatingRepository eventRatingRepository;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    /**
     * Crea un nuevo evento por un organizador
     */
    public void createEvent(Long organizerId, CreateEventRequestDto dto) {
        // Buscar el perfil del organizador en lugar del usuario
        OrganizerProfile organizerProfile = organizerProfileRepository.findByUser_Id(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        // Verificar que el usuario asociado al perfil sea un organizador
        User organizer = organizerProfile.getUser();
        if (organizer.getRole() != Role.ORGANIZADOR) {
            throw new RuntimeException("Solo los organizadores pueden crear eventos");
        }

        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDateTime(dto.getDateTime());
        event.setLocation(dto.getLocation());
        event.setAddress(dto.getAddress());
        event.setCapacity(dto.getCapacity());
        event.setPrice(dto.getPrice());
        event.setDanceStyles(dto.getDanceStyles());
        event.setAdditionalInfo(dto.getAdditionalInfo());
        event.setEventType(dto.getEventType());
        event.setOrganizer(organizerProfile);
        event.setStatus(EventStatus.ACTIVO);
        event.setCancellationReason(null);

        // Procesar las URLs de media
        List<EventMedia> mediaList = new ArrayList<>();
        if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
            mediaList = dto.getMediaUrls().stream()
                    .map(url -> {
                        EventMedia media = new EventMedia();
                        media.setUrl(url);
                        media.setType("image"); // Por defecto
                        media.setEvent(event);
                        return media;
                    })
                    .collect(Collectors.toList());
        }
        event.setMedia(mediaList);

        eventRepository.save(event);
    }

    public void editEvent(Long eventId, Long organizerId, EditEventRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getOrganizer().getUser().getId().equals(organizerId)) {
            throw new RuntimeException("No autorizado para editar este evento.");
        }

        if (event.getStatus() == EventStatus.CANCELADO) {
            throw new RuntimeException("No se puede editar un evento cancelado.");
        }

        // Solo campos editables
        if (dto.getName() != null) event.setName(dto.getName());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getDateTime() != null) event.setDateTime(dto.getDateTime());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getCapacity() != null) event.setCapacity(dto.getCapacity());
        if (dto.getPrice() != null) event.setPrice(dto.getPrice());
        if (dto.getDanceStyles() != null) event.setDanceStyles(dto.getDanceStyles());
        if (dto.getAdditionalInfo() != null) event.setAdditionalInfo(dto.getAdditionalInfo());

        eventRepository.save(event);
    }

    public void cancelEvent(Long eventId, Long organizerId, CancelEventRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getOrganizer().getUser().getId().equals(organizerId)) {
            throw new RuntimeException("No autorizado para cancelar este evento.");
        }

        if (event.getStatus() == EventStatus.CANCELADO) {
            throw new RuntimeException("Este evento ya fue cancelado.");
        }

        event.setStatus(EventStatus.CANCELADO);
        event.setCancellationReason(dto.getCancellationReason());
        eventRepository.save(event);
    }


    /**
     * Obtiene todos los eventos
     */
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los eventos de un organizador específico
     */
    public List<EventResponseDto> getEventsByOrganizer(Long organizerId) {
        OrganizerProfile organizer = organizerProfileRepository.findByUser_Id(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        return eventRepository.findByOrganizer(organizer).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los eventos de un tipo específico
     */
    public List<EventResponseDto> getEventsByType(EventType eventType) {
        return eventRepository.findByEventType(eventType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un evento por su ID
     */
    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        return convertToDto(event);
    }

    /**
     * Califica un evento
     */
    public void rateEvent(Long raterId, EventRatingRequestDto dto) {
        if (raterId == null || dto.getEventId() == null) {
            throw new RuntimeException("IDs inválidos.");
        }

        if (dto.getStars() < 1 || dto.getStars() > 5) {
            throw new RuntimeException("La puntuación debe estar entre 1 y 5 estrellas.");
        }

        if (eventRatingRepository.existsByRaterIdAndEventId(raterId, dto.getEventId())) {
            throw new RuntimeException("Ya has calificado este evento.");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new RuntimeException("Usuario calificador no encontrado"));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Evento a calificar no encontrado"));

        EventRating rating = new EventRating();
        rating.setRater(rater);
        rating.setEvent(event);
        rating.setStars(dto.getStars());
        rating.setComment(dto.getComment());

        eventRatingRepository.save(rating);
    }

    /**
     * Convierte un objeto Event a EventResponseDto
     */
    private EventResponseDto convertToDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setDateTime(event.getDateTime());
        dto.setLocation(event.getLocation());
        dto.setAddress(event.getAddress());
        dto.setCapacity(event.getCapacity());
        dto.setPrice(event.getPrice());
        dto.setDanceStyles(event.getDanceStyles());
        dto.setAdditionalInfo(event.getAdditionalInfo());
        dto.setEventType(event.getEventType());
        dto.setStatus(event.getStatus());
        dto.setCancellationReason(event.getCancellationReason());


        // Información del organizador
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setOrganizerName(event.getOrganizer().getOrganizationName());

        // Media URLs
        List<String> mediaUrls = event.getMedia().stream()
                .map(EventMedia::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);

        // Rating promedio
        dto.setAverageRating(event.getAverageRating());

        return dto;
    }
}
