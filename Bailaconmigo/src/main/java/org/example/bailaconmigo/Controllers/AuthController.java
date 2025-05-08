package org.example.bailaconmigo.Controllers;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Services.AuthService;
import org.example.bailaconmigo.Services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok().body("Usuario registrado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar usuario: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            LoginResponseDto response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar sesión: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok("Se ha enviado un correo con instrucciones para restablecer tu contraseña");
        } catch (RuntimeException e) {
            // Siempre devolvemos un mensaje genérico para evitar que se pueda determinar si el email existe
            return ResponseEntity.ok("Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar la solicitud");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Contraseña restablecida con éxito");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al restablecer la contraseña: " + e.getMessage());
        }
    }

    @PutMapping("/edit/{userId}")
    public ResponseEntity<?> editProfile(@PathVariable Long userId,
                                         @RequestBody EditDancerProfileDto dto) {
        try {
            authService.editProfile(userId, dto);
            return ResponseEntity.ok("Perfil actualizado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public DancerProfileResponseDto getProfileById(@PathVariable Long id) {
        return authService.getProfileById(id);
    }

    @PostMapping("/rate")
    public void rateProfile(@RequestParam Long raterId, @RequestBody RatingRequestDto ratingDto) {
        authService.rateProfile(raterId, ratingDto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DancerProfileResponseDto>> getAllProfiles() {
        List<DancerProfileResponseDto> profiles = authService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/dance-styles")
    public ResponseEntity<DanceStyle[]> getAllDanceStyles() {
        return ResponseEntity.ok(DanceStyle.values());
    }

    @GetMapping("/generar-pago-pro")
    public ResponseEntity<?> generarLinkPago(@RequestParam String email) {
        try {
            // Verificar que el usuario existe y es un bailarín con suscripción BASICO
            User user = authService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            if (user.getRole() != Role.BAILARIN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Solo los bailarines pueden tener membresía PRO"));
            }

            if (user.getSubscriptionType() == SubscriptionType.PRO) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El usuario ya tiene membresía PRO"));
            }

            String link = authService.generarLinkPagoPro(email);
            return ResponseEntity.ok(Map.of("init_point", link));
        } catch (MPException | MPApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo generar el link de pago: " + e.getMessage()));
        }
    }

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<?> webhookMercadoPago(@RequestBody Map<String, Object> payload) {
        try {
            authService.procesarNotificacionPago(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando la notificación: " + e.getMessage()));
        }
    }


}
