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

    @PutMapping("/dancer/edit/{userId}")
    public ResponseEntity<?> editProfile(@PathVariable Long userId,
                                         @RequestBody EditDancerProfileDto dto) {
        try {
            authService.editProfile(userId, dto);
            return ResponseEntity.ok("Perfil actualizado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/dancer/{id}")
    public DancerProfileResponseDto getProfileById(@PathVariable Long id) {
        return authService.getDancerProfileById(id);
    }

    @PostMapping("/rate")
    public void rateProfile(@RequestParam Long raterId, @RequestBody RatingRequestDto ratingDto) {
        authService.rateProfile(raterId, ratingDto);
    }

    @GetMapping("/dancer/all")
    public ResponseEntity<List<DancerProfileResponseDto>> getAllProfiles() {
        List<DancerProfileResponseDto> profiles = authService.getAllDancerProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/dance-styles")
    public ResponseEntity<DanceStyle[]> getAllDanceStyles() {
        return ResponseEntity.ok(DanceStyle.values());
    }

    /**
     * Editar perfil de organizador
     */
    @PutMapping("/organizer/edit/{userId}")
    public ResponseEntity<?> editOrganizerProfile(@PathVariable Long userId,
                                                  @RequestBody EditOrganizerProfileDto dto) {
        try {
            authService.editOrganizerProfile(userId, dto);
            return ResponseEntity.ok("Perfil de organizador actualizado con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el perfil: " + e.getMessage());
        }
    }

    /**
     * Obtener perfil de organizador por user ID
     */
    @GetMapping("/organizer/{userId}")
    public ResponseEntity<?> getOrganizerProfileById(@PathVariable Long userId) {
        try {
            OrganizerProfileResponseDto profile = authService.getOrganizerProfileById(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el perfil: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los perfiles de organizadores
     */
    @GetMapping("/organizer/all")
    public ResponseEntity<List<OrganizerProfileResponseDto>> getAllOrganizerProfiles() {
        try {
            List<OrganizerProfileResponseDto> profiles = authService.getAllOrganizerProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
