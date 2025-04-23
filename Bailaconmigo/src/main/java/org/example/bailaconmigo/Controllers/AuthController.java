package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.DancerProfileResponseDto;
import org.example.bailaconmigo.DTOs.EditDancerProfileDto;
import org.example.bailaconmigo.DTOs.RegisterRequestDto;
import org.example.bailaconmigo.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<DancerProfileResponseDto>> getAllProfiles() {
        List<DancerProfileResponseDto> profiles = authService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }


}
