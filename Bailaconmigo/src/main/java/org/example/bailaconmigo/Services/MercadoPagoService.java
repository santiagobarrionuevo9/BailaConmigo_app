package org.example.bailaconmigo.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.example.bailaconmigo.Configs.JwtTokenUtil;
import org.example.bailaconmigo.DTOs.MercadoPagoTokenResponse;
import org.example.bailaconmigo.Entities.Enum.RegistrationStatus;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.Event;
import org.example.bailaconmigo.Entities.EventRegistration;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MercadoPagoService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EventRegistrationRepository registrationRepository;


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

    @Value("${backend.url}")
    private String backendUrl;

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

        try {
            ResponseEntity<MercadoPagoTokenResponse> response =
                    restTemplate.postForEntity(url, request, MercadoPagoTokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Error en la respuesta de Mercado Pago: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error al intercambiar el código por access_token", e);
            throw new RuntimeException("No se pudo obtener el access_token: " + e.getMessage());
        }
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
                .unitPrice(new BigDecimal("15000.00"))
                .id(referenceId)
                .build();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(email)
                .name(user.getFullName())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/login")
                .failure(frontendUrl + "/pago-rechazado")
                .pending(frontendUrl + "/pago-pendiente")
                .build();

        // Metadata para identificar la transacción
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_id", user.getId());
        metadata.put("subscription_type", "PRO");
        metadata.put("reference_id", referenceId);

        // Corrección de la URL del webhook para que coincida con el controlador
        String notificationUrl = backendUrl + "/api/mercadopago/webhook";
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

    /**
     * Crea una preferencia de pago dividido para un evento específico usando SDK.
     *
     * @param dancer        El usuario que está realizando el pago.
     * @param event         El evento al que se está inscribiendo.
     * @param registrationId El ID de la inscripción.
     * @return El ID de la preferencia de pago creada.
     */
    public String createSplitPaymentPreference(User dancer, Event event, Long registrationId) {
        try {
            // Configurar el token del organizador
            String organizerAccessToken = event.getOrganizer().getUser().getMercadoPagoToken();
            MercadoPagoConfig.setAccessToken(organizerAccessToken);

            BigDecimal totalPrice = BigDecimal.valueOf(event.getPrice());
            BigDecimal marketplaceFee = totalPrice.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);

            // Crear el item de la preferencia
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(event.getId().toString())
                    .title("Inscripción a " + event.getName())
                    .description("Evento de baile - " + event.getAddress())
                    .categoryId("services")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(totalPrice)
                    .build();

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/pago-exito")
                    .pending(frontendUrl + "/pago-pendiente")
                    .failure(frontendUrl + "/pago-rechazado")
                    .build();

            // Crear la preferencia con marketplace_fee
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .backUrls(backUrls)
                    .items(List.of(itemRequest))
                    .externalReference("REG_" + registrationId + "_EVENT_" + event.getId() + "_ORG_" + event.getOrganizer().getId())
                    .notificationUrl(backendUrl + "/api/mercadopago/webhook/inscription-notification")
                    .marketplaceFee(marketplaceFee) // 💰 AQUÍ está la clave - marketplace_fee
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();

        } catch (MPException | MPApiException e) {
            logger.error("Error de Mercado Pago al crear preferencia dividida", e);
            throw new RuntimeException("Error al crear preferencia de pago dividido: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error general al crear preferencia dividida", e);
            throw new RuntimeException("Error inesperado al crear preferencia de pago dividido: " + e.getMessage(), e);
        }
    }

    /**
     * Versión alternativa usando RestTemplate directo (por si prefieres no usar el SDK)
     */
    public String createSplitPaymentPreferenceWithRestTemplate(User dancer, Event event, Long registrationId) {
        try {
            BigDecimal eventPrice = BigDecimal.valueOf(event.getPrice());
            BigDecimal marketplaceFee = eventPrice.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);

            String organizerAccessToken = event.getOrganizer().getUser().getMercadoPagoToken();

            Map<String, Object> body = new HashMap<>();

            // Items
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("title", "Inscripción a " + event.getName());
            item.put("description", "Evento de baile - " + event.getAddress());
            item.put("quantity", 1);
            item.put("currency_id", "ARS");
            item.put("unit_price", eventPrice);
            items.add(item);

            // Payer
            Map<String, Object> payer = new HashMap<>();
            payer.put("name", dancer.getFullName());
            payer.put("email", dancer.getEmail());

            // Back URLs
            Map<String, String> backUrls = Map.of(
                    "success", frontendUrl + "/payment/success?registration=" + registrationId,
                    "failure", frontendUrl + "/payment/failure?registration=" + registrationId,
                    "pending", frontendUrl + "/payment/pending?registration=" + registrationId
            );

            // Armar body completo con marketplace_fee
            body.put("items", items);
            body.put("payer", payer);
            body.put("back_urls", backUrls);
            body.put("auto_return", "approved");
            body.put("external_reference", "REG_" + registrationId + "_EVENT_" + event.getId() + "_ORG_" + event.getOrganizer().getId());
            body.put("notification_url", frontendUrl + "/api/webhooks/mercadopago");
            body.put("marketplace_fee", marketplaceFee); // 💰 CAMBIO CLAVE: marketplace_fee en lugar de application_fee

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(organizerAccessToken); // Token del organizador

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.mercadopago.com/checkout/preferences",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return responseBody.get("id").toString();
            } else {
                throw new RuntimeException("Error creando preferencia: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error al crear preferencia de pago dividido con RestTemplate", e);
            throw new RuntimeException("Error al crear preferencia de pago dividido: " + e.getMessage(), e);
        }
    }

    public BigDecimal calculateAppFee(Double eventPrice) {
        return BigDecimal.valueOf(eventPrice)
                .multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateOrganizerAmount(Double eventPrice) {
        BigDecimal price = BigDecimal.valueOf(eventPrice);
        BigDecimal appFee = calculateAppFee(eventPrice);
        return price.subtract(appFee);
    }

    /**
     * Procesa la notificación recibida de Mercado Pago.
     * Consulta el pago por su ID y actualiza el estado de la inscripción si está aprobado.
     */
    @Transactional
    public void processPaymentNotification(String paymentId) {
        String platformAccessToken = mercadoPagoAccessToken;

        String url = "https://api.mercadopago.com/v1/payments/" + paymentId + "?access_token=" + platformAccessToken;
        ResponseEntity<Map> response;

        try {
            response = restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Esto es para pruebas automáticas de MercadoPago con IDs que no existen
            logger.warn("Pago no encontrado en Mercado Pago (ID: {}): {}", paymentId, e.getMessage());
            return; // No lanzar excepción, solo ignorar notificación inválida
        } catch (Exception e) {
            logger.error("Error al consultar pago con token plataforma: {}", e.getMessage(), e);
            return;
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error("Respuesta no OK desde MercadoPago con token plataforma: {}", response.getStatusCode());
            return;
        }

        Map<String, Object> paymentData = response.getBody();
        String status = (String) paymentData.get("status");
        String externalReference = (String) paymentData.get("external_reference");

        if (externalReference == null) {
            logger.warn("External reference es nulo para pago: {}", paymentId);
            return;
        }

        Long registrationId = parseRegistrationIdFromExternalReference(externalReference);
        Optional<EventRegistration> registrationOpt = registrationRepository.findById(registrationId);
        if (registrationOpt.isEmpty()) {
            logger.warn("Inscripción no encontrada con ID: {}", registrationId);
            return;
        }

        EventRegistration registration = registrationOpt.get();

        String organizerToken = registration.getEvent().getOrganizer().getUser().getMercadoPagoToken();
        url = "https://api.mercadopago.com/v1/payments/" + paymentId + "?access_token=" + organizerToken;

        try {
            response = restTemplate.getForEntity(url, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Pago no encontrado con token organizador (ID: {}): {}", paymentId, e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("Error al consultar pago con token organizador: {}", e.getMessage(), e);
            return;
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error("Respuesta no OK desde MercadoPago con token organizador: {}", response.getStatusCode());
            return;
        }

        paymentData = response.getBody();
        status = (String) paymentData.get("status");

        if ("approved".equalsIgnoreCase(status)) {
            registration.setPaymentStatus("APPROVED");
            registration.setStatus(RegistrationStatus.CONFIRMADO);
            registration.setPaymentId(paymentId);
            registration.setPaymentMethod((String) paymentData.get("payment_method_id"));
            registration.setPaymentDate(LocalDateTime.now());
            registrationRepository.save(registration);

            emailService.sendRegistrationConfirmationToDancer(registration);
            emailService.sendRegistrationNotificationToOrganizer(registration);
        } else {
            logger.info("Pago recibido pero no aprobado (ID: {}, status: {})", paymentId, status);
        }
    }


    private Long parseRegistrationIdFromExternalReference(String externalReference) {
        try {
            // Extraer el número entre "REG_" y "_EVENT"
            int start = externalReference.indexOf("REG_") + 4;
            int end = externalReference.indexOf("_EVENT");
            String idStr = externalReference.substring(start, end);
            return Long.parseLong(idStr);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing externalReference: " + externalReference, e);
        }
    }

    public void refundPayment(String paymentId, String organizerAccessToken) {
        String url = "https://api.mercadopago.com/v1/payments/" + paymentId + "/refunds";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(organizerAccessToken); // token del organizador

        HttpEntity<String> request = new HttpEntity<>("", headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Reembolso exitoso para el pago con ID {}", paymentId);
            } else {
                logger.warn("Error al intentar reembolsar el pago. Status: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al hacer reembolso: {}", e.getResponseBodyAsString());
            throw new RuntimeException("No se pudo realizar el reembolso: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error general al hacer reembolso: {}", e.getMessage(), e);
            throw new RuntimeException("Error al intentar reembolsar el pago: " + e.getMessage(), e);
        }
    }




}