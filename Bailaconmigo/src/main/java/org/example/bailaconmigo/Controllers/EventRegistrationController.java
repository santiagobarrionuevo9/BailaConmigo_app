package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.*;
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

    /**
     * Registro con pago - Para eventos que requieren pago
     */
    @PostMapping("/register-with-payment")
    public ResponseEntity<?> registerForEventWithPayment(@RequestParam Long dancerId,
                                                         @RequestBody EventRegistrationRequestDto registrationDto) {
        try {
            PaymentInitiationResponseDto response = registrationService.registerDancerForEventWithPayment(dancerId, registrationDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la inscripción con pago: " + e.getMessage());
        }
    }

    /**
     * Registro tradicional - Para eventos gratuitos
     */
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

    /**
     * Confirmar pago - Llamado desde el webhook de Mercado Pago
     */
    @PostMapping("/confirm-payment/{registrationId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long registrationId,
                                            @RequestParam String paymentId,
                                            @RequestParam String paymentStatus,
                                            @RequestParam(required = false) String paymentMethod,
                                            @RequestParam(required = false) String paymentDetails) {
        try {
            registrationService.confirmPayment(registrationId, paymentId, paymentStatus, paymentMethod, paymentDetails);
            return ResponseEntity.ok("Pago confirmado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al confirmar el pago: " + e.getMessage());
        }
    }

    /**
     * Cancelar inscripción
     */
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

    /**
     * Actualiza el estado de asistencia de una inscripción
     * PUT /api/registrations/{registrationId}/attendance
     */
    @PutMapping("/{registrationId}/attendance")
    public ResponseEntity<EventRegistrationResponseDto> updateAttendance(
            @PathVariable Long registrationId,
            @RequestBody UpdateAttendanceRequestDto dto) {

        try {
            EventRegistrationResponseDto updatedRegistration =
                    registrationService.updateAttendance(registrationId, dto);

            return ResponseEntity.ok(updatedRegistration);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }
}