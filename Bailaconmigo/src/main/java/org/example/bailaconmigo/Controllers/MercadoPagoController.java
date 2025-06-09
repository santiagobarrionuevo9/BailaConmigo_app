package org.example.bailaconmigo.Controllers;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bailaconmigo.DTOs.MercadoPagoTokenResponse;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.example.bailaconmigo.Services.AuthService;
import org.example.bailaconmigo.Services.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
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

    @Autowired
    private AuthService userService;

    /**
     * Endpoint para recibir el código de autorización de MercadoPago
     * y intercambiarlo por un access token
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleMercadoPagoCallback(
            @RequestParam("code") String authorizationCode,
            @RequestParam("state") String userId, // El ID del usuario que inició el proceso
            HttpServletResponse response) {

        try {
            // 1. Intercambiar el código por un access token
            String accessToken = exchangeCodeForToken(authorizationCode);

            // 2. Guardar el token en la base de datos
            userService.saveMercadoPagoToken(Long.parseLong(userId), accessToken);

            // 3. Redireccionar al frontend con éxito
            response.sendRedirect("https://c7bb-152-171-81-105.ngrok-free.app/vinculacion-exitosa");

            return null; // No necesitamos retornar nada porque redireccionamos

        } catch (Exception e) {
            try {
                // Redireccionar al frontend con error
                response.sendRedirect("https://c7bb-152-171-81-105.ngrok-free.app/vinculacion-rechazado");
            } catch (IOException ioException) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error en la vinculación con MercadoPago"));
            }
            return null;
        }
    }

    /**
     * Método para intercambiar el código de autorización por un access token
     */
    private String exchangeCodeForToken(String authorizationCode) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // URL del endpoint de MercadoPago para intercambiar código por token
        String tokenUrl = "https://api.mercadopago.com/oauth/token";

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Body de la petición
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("client_id", "3552235923798667"); // Reemplaza con tu Client ID
        requestBody.put("client_secret", "wqwqsitqHVrlHFwvHTx6EId1mh99YPRZ"); // Reemplaza con tu Client Secret
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("code", authorizationCode);
        requestBody.put("redirect_uri", "https://6d8b-152-171-81-105.ngrok-free.app/api/mercadopago/callback"); // Tu redirect URI

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // Hacer la petición
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Error al obtener el access token de MercadoPago");
        }
    }

    /**
     * Endpoint opcional para iniciar el proceso de OAuth (genera la URL de autorización)
     */
    @PostMapping("/connect/{userId}")
    public ResponseEntity<Map<String, String>> generateAuthUrl(@PathVariable Long userId) {

        String clientId = "3552235923798667"; // Reemplaza con tu Client ID
        String redirectUri = "https://6d8b-152-171-81-105.ngrok-free.app/api/mercadopago/callback";

        String authUrl = String.format(
                "https://auth.mercadopago.com/authorization?client_id=%s&response_type=code&platform_id=mp&state=%s&redirect_uri=%s",
                clientId, userId.toString(), redirectUri
        );

        return ResponseEntity.ok(Map.of("authUrl", authUrl));
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

    @PostMapping("/webhook/inscription-notification")
    public ResponseEntity<String> handleMercadoPagoNotification(@RequestBody Map<String, Object> payload) {
        try {
            // Mercado Pago envía el campo "type" y "data" en el body del webhook
            String type = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            if ("payment".equalsIgnoreCase(type) && data != null) {
                String paymentId = String.valueOf(data.get("id"));

                // Procesar el pago recibido
                mercadoService.processPaymentNotification(paymentId);

                return ResponseEntity.ok("Notification processed");
            } else {
                return ResponseEntity.badRequest().body("Unsupported notification type");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing notification: " + e.getMessage());
        }
    }
}

