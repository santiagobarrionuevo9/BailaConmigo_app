package org.example.bailaconmigo.Controllers;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.example.bailaconmigo.DTOs.MercadoPagoTokenResponse;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.example.bailaconmigo.Services.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    @Autowired
    private MercadoPagoService mercadoService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            Principal principal // esto te da el usuario logueado
    ) {
        try {
            // 1. Obtener el token de Mercado Pago
            MercadoPagoTokenResponse token = mercadoService.exchangeCodeForAccessToken(code);

            // 2. Buscar al usuario por email (ajustá esto a tu lógica de autenticación)
            String email = principal.getName(); // funciona si usás Spring Security con email como username
            Optional<User> optionalUsuario = userRepository.findByEmail(email);

            if (optionalUsuario.isPresent()) {
                User usuario = optionalUsuario.get();

                // 3. Guardar el token en el usuario
                usuario.setMercadoPagoToken(token.getAccess_token());
                userRepository.save(usuario);

                return ResponseEntity.ok("Cuenta de Mercado Pago vinculada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al vincular Mercado Pago: " + e.getMessage());
        }
    }

    @GetMapping("/generar-pago-pro")
    public ResponseEntity<?> generarLinkPago(@RequestParam String email) {
        try {
            // Verificar que el usuario existe y es un bailarín con suscripción BASICO
            User user = mercadoService.getUserByEmail(email);
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

            String link = mercadoService.generarLinkPagoPro(email);
            return ResponseEntity.ok(Map.of("init_point", link));
        } catch (MPException | MPApiException e) {
            logger.error("Error generating payment link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo generar el link de pago: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhookMercadoPago(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("Received webhook from MercadoPago: {}", payload);
            mercadoService.procesarNotificacionPago(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing webhook notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error procesando la notificación: " + e.getMessage()));
        }
    }
}
