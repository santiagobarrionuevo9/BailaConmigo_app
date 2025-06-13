package org.example.bailaconmigo.Services;

import jakarta.transaction.Transactional;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.*;
import org.example.bailaconmigo.Entities.Enum.EventStatus;
import org.example.bailaconmigo.Entities.Enum.EventType;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Repositories.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private EmailService emailService;

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
        event.setAddress(dto.getAddress());
        event.setCapacity(dto.getCapacity());
        event.setPrice(dto.getPrice());
        event.setDanceStyles(dto.getDanceStyles());
        event.setAdditionalInfo(dto.getAdditionalInfo());
        event.setEventType(dto.getEventType());
        event.setOrganizer(organizerProfile);
        event.setStatus(EventStatus.ACTIVO);
        event.setCancellationReason(null);

        // Configurar city y country
        if (dto.getCityId() != null) {
            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
            event.setCity(city);
        }

        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("País no encontrado"));
            event.setCountry(country);
        }

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

    @Transactional
    public void editEvent(Long eventId, Long organizerId, EditEventRequestDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getOrganizer().getUser().getId().equals(organizerId)) {
            throw new RuntimeException("No autorizado para editar este evento.");
        }

        if (event.getStatus() == EventStatus.CANCELADO) {
            throw new RuntimeException("No se puede editar un evento cancelado.");
        }

        // Guardar datos originales para comparar cambios
        String oldCityName = event.getCityName();
        String oldCountryName = event.getCountryName();
        String oldAddress = event.getAddress();
        LocalDateTime oldDateTime = event.getDateTime();

        // Solo campos editables
        if (dto.getName() != null) event.setName(dto.getName());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getDateTime() != null) event.setDateTime(dto.getDateTime());
        if (dto.getAddress() != null) event.setAddress(dto.getAddress());
        if (dto.getCapacity() != null) event.setCapacity(dto.getCapacity());
        if (dto.getPrice() != null) event.setPrice(dto.getPrice());
        if (dto.getDanceStyles() != null) event.setDanceStyles(dto.getDanceStyles());
        if (dto.getAdditionalInfo() != null) event.setAdditionalInfo(dto.getAdditionalInfo());

        // Actualizar city y country si se proporcionan
        if (dto.getCityId() != null) {
            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
            event.setCity(city);
        }

        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("País no encontrado"));
            event.setCountry(country);
        }

        eventRepository.save(event);

        // Construir mensaje con los cambios realizados
        StringBuilder changes = new StringBuilder();
        if (dto.getDateTime() != null && !dto.getDateTime().equals(oldDateTime)) {
            changes.append("- Fecha y hora: ").append(dto.getDateTime()).append("\n");
        }
        if (dto.getCityId() != null && !event.getCityName().equals(oldCityName)) {
            changes.append("- Ciudad: ").append(event.getCityName()).append("\n");
        }
        if (dto.getCountryId() != null && !event.getCountryName().equals(oldCountryName)) {
            changes.append("- País: ").append(event.getCountryName()).append("\n");
        }
        if (dto.getAddress() != null && !dto.getAddress().equals(oldAddress)) {
            changes.append("- Dirección: ").append(dto.getAddress()).append("\n");
        }
        if (dto.getCapacity() != null) {
            changes.append("- Capacidad: ").append(dto.getCapacity()).append("\n");
        }
        if (dto.getPrice() != null) {
            changes.append("- Precio: ").append(dto.getPrice()).append("\n");
        }

        // Notificar cambios a los inscritos si hay cambios importantes
        if (changes.length() > 0) {
            emailService.notifyEventUpdate(event, changes.toString());
        }
    }

    @Transactional
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

        // Notificar a los inscritos sobre la cancelación
        emailService.notifyEventCancellation(event, dto.getCancellationReason());
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
     * Obtiene eventos por ciudad
     */
    public List<EventResponseDto> getEventsByCity(Long cityId) {
        return eventRepository.findByCityId(cityId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos por país
     */
    public List<EventResponseDto> getEventsByCountry(Long countryId) {
        return eventRepository.findByCountryId(countryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos por ciudad y país
     */
    public List<EventResponseDto> getEventsByCityAndCountry(Long cityId, Long countryId) {
        return eventRepository.findByCityIdAndCountryId(cityId, countryId).stream()
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
        dto.setAddress(event.getAddress());
        dto.setCapacity(event.getCapacity());
        dto.setAvailableCapacity(event.getAvailableCapacity());
        dto.setPrice(event.getPrice());
        dto.setDanceStyles(event.getDanceStyles());
        dto.setAdditionalInfo(event.getAdditionalInfo());
        dto.setEventType(event.getEventType());
        dto.setStatus(event.getStatus());
        dto.setCancellationReason(event.getCancellationReason());

        // Información del organizador
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setOrganizerName(event.getOrganizer().getOrganizationName());

        // Información de ubicación
        if (event.getCity() != null) {
            dto.setCityId(event.getCity().getId());
            dto.setCityName(event.getCity().getName());
        }

        if (event.getCountry() != null) {
            dto.setCountryId(event.getCountry().getId());
            dto.setCountryName(event.getCountry().getName());
        }

        // Media URLs
        List<String> mediaUrls = event.getMedia().stream()
                .map(EventMedia::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);

        // Rating promedio
        dto.setAverageRating(event.getAverageRating());

        return dto;
    }

    /**
     * Busca eventos que necesitan recordatorio de 24 horas y envía las notificaciones
     * Este método debería ser llamado por un job programado
     */
    @Transactional
    public void sendReminders24Hours() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24);

        // Buscar eventos que ocurren en las próximas 24 horas (con un margen de 1 hora)
        LocalDateTime startRange = reminderTime.minusHours(1);
        LocalDateTime endRange = reminderTime.plusHours(1);

        List<Event> eventsToRemind = eventRepository.findByDateTimeBetweenAndStatusWithOrganizer(
                startRange, endRange, EventStatus.ACTIVO);


        System.out.println("Buscando eventos para recordatorio 24h entre " +
                startRange.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                " y " + endRange.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        if (eventsToRemind.isEmpty()) {
            System.out.println("No se encontraron eventos para enviar recordatorios 24h");
            return;
        }

        for (Event event : eventsToRemind) {
            Hibernate.initialize(event.getRegistrations());
            {
                try {
                    if (event.getRegistrations() != null && !event.getRegistrations().isEmpty()) {
                        emailService.sendEventReminder24Hours(event);
                    } else {
                        System.out.println("Evento sin inscripciones: " + event.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Error enviando recordatorios para evento " +
                            event.getName() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Proceso de recordatorios 24h completado. Eventos procesados: " + eventsToRemind.size());
    }

    /**
     * Método manual para enviar recordatorio de un evento específico
     */
    @Transactional
    public void sendReminderForSpecificEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (event.getStatus() != EventStatus.ACTIVO) {
            throw new RuntimeException("Solo se pueden enviar recordatorios para eventos activos");
        }

        if (event.getRegistrations() == null || event.getRegistrations().isEmpty()) {
            throw new RuntimeException("El evento no tiene inscripciones");
        }

        emailService.sendEventReminder24Hours(event);
    }

    /**
     * Se ejecuta cada hora para verificar si hay eventos que necesiten recordatorio 24h
     * Cron expression: "0 0 * * * *" = cada hora en punto
     */
    @Scheduled(cron = "0 */5 * * * *", zone = "America/Argentina/Cordoba")
    public void sendDailyReminders() {
        try {
            System.out.println("Iniciando proceso automático de recordatorios 24h...");
            sendReminders24Hours();
        } catch (Exception e) {
            System.out.println("Error en proceso automático de recordatorios: " + e.getMessage());
        }
    }


}
