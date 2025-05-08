package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestParam Long organizerId,
                                         @RequestBody CreateEventRequestDto eventRequest) {
        try {
            eventService.createEvent(organizerId, eventRequest);
            return ResponseEntity.ok().body("Evento creado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el evento: " + e.getMessage());
        }
    }

    @PutMapping("/{eventId}/edit")
    public void editEvent(@PathVariable Long eventId,
                          @RequestParam Long organizerId,
                          @RequestBody EditEventRequestDto dto) {
        eventService.editEvent(eventId, organizerId, dto);
    }

    // PUT para cancelar evento
    @PutMapping("/{eventId}/cancel")
    public void cancelEvent(@PathVariable Long eventId,
                            @RequestParam Long organizerId,
                            @RequestBody CancelEventRequestDto dto) {
        eventService.cancelEvent(eventId, organizerId, dto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        List<EventResponseDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getEventsByOrganizer(@PathVariable Long organizerId) {
        try {
            List<EventResponseDto> events = eventService.getEventsByOrganizer(organizerId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener eventos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            EventResponseDto event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el evento: " + e.getMessage());
        }
    }

    @PostMapping("/rate")
    public ResponseEntity<?> rateEvent(@RequestParam Long raterId,
                                       @RequestBody EventRatingRequestDto ratingDto) {
        try {
            eventService.rateEvent(raterId, ratingDto);
            return ResponseEntity.ok("Calificación guardada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calificar el evento: " + e.getMessage());
        }
    }
}
