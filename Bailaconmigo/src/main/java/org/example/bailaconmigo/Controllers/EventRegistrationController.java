package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.CancelRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationRequestDto;
import org.example.bailaconmigo.DTOs.EventRegistrationResponseDto;
import org.example.bailaconmigo.Services.EventRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-registrations")
public class EventRegistrationController {

    @Autowired
    private EventRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(@RequestParam Long dancerId,
                                              @RequestBody EventRegistrationRequestDto registrationDto) {
        try {
            EventRegistrationResponseDto response = registrationService.registerDancerForEvent(dancerId, registrationDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrarse en el evento: " + e.getMessage());
        }
    }

    @PutMapping("/{eventId}/cancel")
    public ResponseEntity<?> cancelRegistration(@RequestParam Long dancerId,
                                                @PathVariable Long eventId,
                                                @RequestBody CancelRegistrationRequestDto dto) {
        try {
            registrationService.cancelRegistration(dancerId, eventId, dto);
            return ResponseEntity.ok("Inscripción cancelada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cancelar la inscripción: " + e.getMessage());
        }
    }

    @GetMapping("/dancer/{dancerId}")
    public ResponseEntity<?> getRegistrationsByDancer(@PathVariable Long dancerId) {
        try {
            List<EventRegistrationResponseDto> registrations = registrationService.getRegistrationsByDancer(dancerId);
            return ResponseEntity.ok(registrations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las inscripciones: " + e.getMessage());
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getRegistrationsByEvent(@PathVariable Long eventId) {
        try {
            List<EventRegistrationResponseDto> registrations = registrationService.getRegistrationsByEvent(eventId);
            return ResponseEntity.ok(registrations);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las inscripciones: " + e.getMessage());
        }
    }
}