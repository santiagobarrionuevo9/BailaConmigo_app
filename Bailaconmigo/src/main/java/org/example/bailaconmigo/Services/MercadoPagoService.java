package org.example.bailaconmigo.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.example.bailaconmigo.Configs.JwtTokenUtil;
import org.example.bailaconmigo.DTOs.MercadoPagoTokenResponse;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.DancerProfileRepository;
import org.example.bailaconmigo.Repositories.OrganizerProfileRepository;
import org.example.bailaconmigo.Repositories.RatingRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class MercadoPagoService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;


    private final RestTemplate restTemplate;

    public MercadoPagoService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Value("${mercadopago.client-id}")
    private String clientId;

    @Value("${mercadopago.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.redirect-uri}")
    private String redirectUri;


    @Value("${mercadopago.access.token}")
    private String mercadoPagoAccessToken;

    @Value("${frontend.url}")
    private String frontendUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public MercadoPagoTokenResponse exchangeCodeForAccessToken(String code) {
        String url = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<MercadoPagoTokenResponse> response = restTemplate.postForEntity(url, request, MercadoPagoTokenResponse.class);

        return response.getBody();
    }

    @PostConstruct
    public void initMercadoPago() {
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public String generarLinkPagoPro(String email) throws MPException, MPApiException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear referencia única para este pago
        String referenceId = "pro_sub_" + user.getId() + "_" + System.currentTimeMillis();

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Suscripción PRO - Baila Conmigo")
                .description("Acceso a funcionalidades premium por 3 meses")
                .quantity(1)
                .unitPrice(new BigDecimal("1.00"))
                .id(referenceId)
                .build();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(email)
                .name(user.getFullName())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/perfil?status=success")
                .failure(frontendUrl + "/perfil?status=failure")
                .pending(frontendUrl + "/perfil?status=pending")
                .build();

        // Metadata para identificar la transacción
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_id", user.getId());
        metadata.put("subscription_type", "PRO");
        metadata.put("reference_id", referenceId);

        // Corrección de la URL del webhook para que coincida con el controlador
        String notificationUrl = frontendUrl + "/api/mercadopago/webhook";
        logger.info("Notification URL configured as: {}", notificationUrl);

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(referenceId)
                .metadata(metadata)
                .notificationUrl(notificationUrl)
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        // Guardar referencia para validación posterior
        user.setLastPaymentReference(referenceId);
        userRepository.save(user);

        logger.info("Payment preference created with init point: {}", preference.getInitPoint());
        return preference.getInitPoint();
    }


    public void procesarNotificacionPago(Map<String, Object> payload) {
        logger.info("Received payment notification: {}", payload);
        String type = (String) payload.get("type");

        // Solo procesar notificaciones de pagos
        if ("payment".equals(type)) {
            String id = null;
            // Extract the payment ID correctly depending on the payload structure
            if (payload.containsKey("data") && payload.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                id = String.valueOf(data.get("id"));
            } else if (payload.containsKey("data.id")) {
                id = String.valueOf(payload.get("data.id"));
            }

            if (id == null) {
                logger.error("Payment ID not found in payload");
                return;
            }

            // Verificar si es una notificación de prueba (IDs conocidos de prueba)
            boolean isTestNotification = "123456".equals(id) || "test".equals(payload.get("live_mode")) || Boolean.FALSE.equals(payload.get("live_mode"));

            if (isTestNotification) {
                logger.info("Detectada notificación de prueba. ID: {}", id);
                logger.info("La notificación de prueba ha sido procesada exitosamente");
                return; // No procesar más para notificaciones de prueba
            }

            // Obtener detalles del pago desde Mercado Pago (solo para pagos reales)
            try {
                logger.info("Processing payment with ID: {}", id);
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.valueOf(id));

                // Verificar estado del pago
                logger.info("Payment status: {}", payment.getStatus());
                if ("approved".equals(payment.getStatus())) {
                    String referenceId = payment.getExternalReference();
                    logger.info("Payment approved with reference: {}", referenceId);

                    // Buscar usuario por referencia de pago
                    Optional<User> userOpt = userRepository.findByLastPaymentReference(referenceId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        logger.info("User found: {}", user.getEmail());

                        // Activar suscripción PRO
                        user.setSubscriptionType(SubscriptionType.PRO);
                        user.setSubscriptionExpiration(LocalDate.now().plusMonths(3));
                        user.setLastPaymentReference(null); // Limpiar referencia
                        userRepository.save(user);
                        logger.info("PRO subscription activated for user: {}", user.getEmail());

                        // Notificar al usuario
                        emailService.sendSubscriptionConfirmationEmail(
                                user.getEmail(),
                                user.getFullName(),
                                user.getSubscriptionExpiration()
                        );
                        logger.info("Confirmation email sent to user: {}", user.getEmail());
                    } else {
                        logger.warn("No user found with payment reference: {}", referenceId);
                    }
                }
            } catch (Exception e) {
                // Registrar error pero no reenviar excepción para evitar reintento de MP
                logger.error("Error procesando pago: " + e.getMessage(), e);
            }
        } else {
            logger.info("Ignoring non-payment notification of type: {}", type);
        }
    }
}