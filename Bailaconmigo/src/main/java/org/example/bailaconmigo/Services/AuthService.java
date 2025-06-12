package org.example.bailaconmigo.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;


import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.PreferenceItem;
import com.mercadopago.resources.preference.PreferencePayer;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.example.bailaconmigo.Configs.JwtTokenUtil;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.*;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DancerProfileRepository profileRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${mercadopago.access.token}")
    private String mercadoPagoAccessToken;

    @Value("${frontend.url}")
    private String frontendUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Registra un nuevo usuario en la base de datos.
     */
    public void register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validación de edad mínima
        if (request.getBirthdate() != null) {
            int edad = LocalDate.now().getYear() - request.getBirthdate().getYear();
            if (request.getBirthdate().plusYears(edad).isAfter(LocalDate.now())) {
                edad--;
            }
            if (edad < 18) {
                throw new RuntimeException("Debes tener al menos 18 años para registrarte.");
            }
        }

        // Buscar ciudad y país
        City city = null;
        Country country = null;

        if (request.getCityId() != null) {
            city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
        }

        if (request.getCountryId() != null) {
            country = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new RuntimeException("País no encontrado"));
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthdate(request.getBirthdate());
        user.setCity(city);
        user.setCountry(country);
        user.setRole(request.getRole());

        if (request.getRole() == Role.BAILARIN) {
            SubscriptionType type = request.getSubscriptionType() != null ? request.getSubscriptionType() : SubscriptionType.BASICO;
            if (type != SubscriptionType.BASICO && type != SubscriptionType.PRO) {
                throw new RuntimeException("Solo BASICO o PRO para bailarines");
            }

            user.setSubscriptionType(type);
            user.setSubscriptionExpiration(type == SubscriptionType.PRO
                    ? LocalDate.now().plusMonths(3)
                    : LocalDate.now().plusDays(15));
        } else {
            user.setSubscriptionType(SubscriptionType.SIN_SUSCRIPCION);
            user.setSubscriptionExpiration(null);
        }

        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        if (user.getRole() == Role.BAILARIN) {
            DancerProfile profile = new DancerProfile();
            profile.setUser(user);
            profile.setFullName(user.getFullName());

            int edad = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
            profile.setAge(edad);

            profile.setDanceStyles(new HashSet<>());
            profile.setLevel(null);
            profile.setAboutMe("");
            profile.setAvailability("");
            profile.setMedia(new ArrayList<>());

            profileRepository.save(profile);
        }
        else if (user.getRole() == Role.ORGANIZADOR) {
            OrganizerProfile organizerProfile = new OrganizerProfile();
            organizerProfile.setUser(user);
            organizerProfile.setOrganizationName(user.getFullName());
            organizerProfile.setContactEmail(user.getEmail());
            organizerProfile.setContactPhone("");
            organizerProfile.setDescription("");
            organizerProfile.setWebsite("");
            organizerProfile.setEvents(new ArrayList<>());
            organizerProfile.setMedia(new ArrayList<>());

            organizerProfileRepository.save(organizerProfile);
        }
    }

    /**
     * Permite a un usuario iniciar sesión y obtener un token JWT
     */
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtTokenUtil.generateToken(user.getEmail(), user.getId(), user.getRole().toString());

        return new LoginResponseDto(
                token,
                user.getId(),
                user.getFullName(),
                user.getRole().toString(),
                user.getSubscriptionType().toString(),
                user.getSubscriptionExpiration(),
                user.getEmail()
        );
    }

    /**
     * Inicia el proceso de recuperación de contraseña
     */
    public void forgotPassword(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email no registrado"));

        String resetToken = jwtTokenUtil.generatePasswordResetToken(user.getEmail());

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
    }

    /**
     * Restablece la contraseña con el token proporcionado
     */
    public void resetPassword(ResetPasswordRequestDto request) {
        if (!jwtTokenUtil.validateToken(request.getToken())) {
            throw new RuntimeException("Token inválido o expirado");
        }

        String email = jwtTokenUtil.getEmailFromToken(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!request.getToken().equals(user.getPasswordResetToken()) ||
                user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token inválido o expirado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
    }

    /**
     * Edita el perfil de un bailarín
     */
    public void editProfile(Long userId, EditDancerProfileDto dto) {
        DancerProfile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        User user = profile.getUser();

        // Actualizar ubicación si se proporciona
        if (dto.getCityId() != null) {
            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
            user.setCity(city);
        }

        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("País no encontrado"));
            user.setCountry(country);
        }

        // Guardar cambios en user si se modificó ubicación
        if (dto.getCityId() != null || dto.getCountryId() != null) {
            userRepository.save(user);
        }

        // Actualizar campos del perfil
        profile.setDanceStyles(dto.getDanceStyles());
        profile.setLevel(dto.getLevel());
        profile.setAboutMe(dto.getAboutMe());
        profile.setAvailability(dto.getAvailability());

        // Manejo de media
        List<String> currentMediaUrls = profile.getMedia().stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());

        if (dto.getMediaUrls() != null) {
            List<Media> newMediaItems = dto.getMediaUrls().stream()
                    .filter(url -> !currentMediaUrls.contains(url))
                    .map(url -> {
                        Media media = new Media();
                        media.setUrl(url);
                        media.setProfile(profile);
                        return media;
                    })
                    .collect(Collectors.toList());

            if (!newMediaItems.isEmpty()) {
                profile.getMedia().addAll(newMediaItems);
            }
        }

        profileRepository.save(profile);
    }

    /**
     * Edita el perfil de un organizador
     */
    public void editOrganizerProfile(Long userId, EditOrganizerProfileDto dto) {
        OrganizerProfile profile = organizerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de organizador no encontrado"));

        User user = profile.getUser();

        // Actualizar ubicación del usuario si se proporciona
        if (dto.getCityId() != null) {
            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ciudad no encontrada"));
            user.setCity(city);
        }

        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("País no encontrado"));
            user.setCountry(country);
        }

        // Guardar cambios en user si se modificó ubicación
        if (dto.getCityId() != null || dto.getCountryId() != null) {
            userRepository.save(user);
        }

        // Actualizar campos del perfil del organizador
        profile.setOrganizationName(dto.getOrganizationName());
        profile.setContactEmail(dto.getContactEmail());
        profile.setContactPhone(dto.getContactPhone());
        profile.setDescription(dto.getDescription());
        profile.setWebsite(dto.getWebsite());

        // Manejo de media
        List<String> currentMediaUrls = profile.getMedia().stream()
                .map(OrganizerMedia::getUrl)
                .collect(Collectors.toList());

        if (dto.getMediaUrls() != null) {
            List<OrganizerMedia> newMediaItems = dto.getMediaUrls().stream()
                    .filter(url -> !currentMediaUrls.contains(url))
                    .map(url -> {
                        OrganizerMedia media = new OrganizerMedia();
                        media.setUrl(url);
                        media.setProfile(profile);
                        return media;
                    })
                    .collect(Collectors.toList());

            if (!newMediaItems.isEmpty()) {
                profile.getMedia().addAll(newMediaItems);
            }
        }

        organizerProfileRepository.save(profile);
    }

    /**
     * Califica un perfil
     */
    @Transactional
    public void rateProfile(Long raterId, RatingRequestDto dto) {
        if (raterId == null || dto.getProfileId() == null) {
            throw new RuntimeException("IDs inválidos.");
        }

        if (dto.getStars() < 1 || dto.getStars() > 5) {
            throw new RuntimeException("La puntuación debe estar entre 1 y 5 estrellas.");
        }

        if (ratingRepository.existsByRaterIdAndProfileId(raterId, dto.getProfileId())) {
            throw new RuntimeException("Ya has calificado a este perfil.");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new RuntimeException("Usuario calificador no encontrado"));

        DancerProfile profile = profileRepository.findByUser_Id(dto.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil a calificar no encontrado"));

        Rating rating = new Rating();
        rating.setRater(rater);
        rating.setProfile(profile);
        rating.setStars(dto.getStars());
        rating.setComment(dto.getComment());

        // Guardar el rating primero
        ratingRepository.save(rating);

        // Agregar el rating a la lista del profile para mantener la relación bidireccional
        profile.getRatings().add(rating);

        // Guardar el profile actualizado
        profileRepository.save(profile);
    }

    /**
     * Obtiene el perfil de un bailarín por ID de usuario
     */
    public DancerProfileResponseDto getDancerProfileById(Long id) {
        // Usar el método que carga los ratings
        DancerProfile profile = profileRepository.findByUser_IdWithRatings(id)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        DancerProfileResponseDto dto = new DancerProfileResponseDto();
        dto.setUserId(id);
        dto.setFullName(profile.getFullName());
        dto.setAge(profile.getAge());
        dto.setCityId(profile.getUser().getCity() != null ? profile.getUser().getCity().getId() : null);
        dto.setCountryId(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getId() : null);
        // Obtener nombres de ciudad y país desde las entidades
        dto.setCityName(profile.getUser().getCity() != null ? profile.getUser().getCity().getName() : null);
        dto.setCountryName(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getName() : null);

        dto.setDanceStyles(profile.getDanceStyles());
        dto.setLevel(profile.getLevel());
        dto.setAboutMe(profile.getAboutMe());
        dto.setAvailability(profile.getAvailability());
        dto.setSubscriptionType(profile.getUser().getSubscriptionType());

        List<String> mediaUrls = profile.getMedia().stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);

        // Ahora debería calcular correctamente el promedio
        dto.setAverageRating(profile.getAverageRating());

        // Debug: agregar log para verificar
        logger.info("Profile ID: {}, Number of ratings: {}, Average: {}",
                profile.getId(), profile.getRatings().size(), profile.getAverageRating());

        return dto;
    }

    /**
     * Obtiene todos los perfiles de bailarines
     */
    public List<DancerProfileResponseDto> getAllDancerProfiles() {
        return profileRepository.findAll().stream().map(profile -> {
            User user = profile.getUser();
            DancerProfileResponseDto dto = new DancerProfileResponseDto();
            dto.setUserId(user.getId());
            dto.setFullName(user.getFullName());

            int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
            dto.setAge(age);

            // Obtener nombres de ciudad y país desde las entidades
            dto.setCityId(user.getCity() != null ? user.getCity().getId() : null);
            dto.setCountryId(user.getCountry() != null ? user.getCountry().getId() : null);
            dto.setCityName(user.getCity() != null ? user.getCity().getName() : null);
            dto.setCountryName(user.getCountry() != null ? user.getCountry().getName() : null);

            dto.setDanceStyles(profile.getDanceStyles());
            dto.setLevel(profile.getLevel());
            dto.setAboutMe(profile.getAboutMe());
            dto.setAvailability(profile.getAvailability());
            dto.setSubscriptionType(user.getSubscriptionType());

            List<String> mediaUrls = profile.getMedia().stream()
                    .map(Media::getUrl)
                    .collect(Collectors.toList());
            dto.setMediaUrls(mediaUrls);

            dto.setAverageRating(profile.getAverageRating());

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene el perfil de un organizador por user ID
     */
    public OrganizerProfileResponseDto getOrganizerProfileById(Long userId) {
        OrganizerProfile profile = organizerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de organizador no encontrado"));

        OrganizerProfileResponseDto dto = new OrganizerProfileResponseDto();
        dto.setId(profile.getId());
        dto.setOrganizationName(profile.getOrganizationName());
        dto.setContactEmail(profile.getContactEmail());
        dto.setContactPhone(profile.getContactPhone());
        dto.setDescription(profile.getDescription());
        dto.setWebsite(profile.getWebsite());
        dto.setFullName(profile.getUser().getFullName());

        // Obtener nombres de ciudad y país desde las entidades
        dto.setCityId(profile.getUser().getCity() != null ? profile.getUser().getCity().getId() : null);
        dto.setCountryId(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getId() : null);

        dto.setCityName(profile.getUser().getCity() != null ? profile.getUser().getCity().getName() : null);
        dto.setCountryName(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getName() : null);

        List<String> mediaUrls = profile.getMedia().stream()
                .map(OrganizerMedia::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);

        return dto;
    }

    /**
     * Obtiene todos los perfiles de organizadores
     */
    public List<OrganizerProfileResponseDto> getAllOrganizerProfiles() {
        return organizerProfileRepository.findAll().stream().map(profile -> {
            OrganizerProfileResponseDto dto = new OrganizerProfileResponseDto();
            dto.setId(profile.getId());
            dto.setOrganizationName(profile.getOrganizationName());
            dto.setContactEmail(profile.getContactEmail());
            dto.setContactPhone(profile.getContactPhone());
            dto.setDescription(profile.getDescription());
            dto.setWebsite(profile.getWebsite());
            dto.setFullName(profile.getUser().getFullName());
            dto.setCityId(profile.getUser().getCity() != null ? profile.getUser().getCity().getId() : null);
            dto.setCountryId(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getId() : null);

            // Obtener nombres de ciudad y país desde las entidades
            dto.setCityName(profile.getUser().getCity() != null ? profile.getUser().getCity().getName() : null);
            dto.setCountryName(profile.getUser().getCountry() != null ? profile.getUser().getCountry().getName() : null);

            List<String> mediaUrls = profile.getMedia().stream()
                    .map(OrganizerMedia::getUrl)
                    .collect(Collectors.toList());
            dto.setMediaUrls(mediaUrls);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void saveMercadoPagoToken(Long userId, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setMercadoPagoToken(accessToken);
        userRepository.save(user);
    }
}