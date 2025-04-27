package org.example.bailaconmigo.Services;

import org.example.bailaconmigo.Configs.JwtTokenUtil;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.*;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Repositories.DancerProfileRepository;
import org.example.bailaconmigo.Repositories.OrganizerProfileRepository;
import org.example.bailaconmigo.Repositories.RatingRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private EmailService emailService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param request Objeto que contiene los datos del nuevo usuario.
     * @throws RuntimeException Si el email ya está registrado o si el tipo de suscripción no es válido.
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

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthdate(request.getBirthdate());
        user.setCity(request.getCity());
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

            profile.setCity(user.getCity());

            // El resto se deja vacío inicialmente
            profile.setDanceStyles(new HashSet<>()); // Vacío
            profile.setLevel(null); // A definir luego
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
            organizerProfile.setContactPhone("");  // Vacío inicialmente
            organizerProfile.setDescription("");   // Vacío inicialmente
            organizerProfile.setWebsite("");       // Vacío inicialmente
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
                user.getRole().toString()
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
        // Verificar que el token sea válido
        if (!jwtTokenUtil.validateToken(request.getToken())) {
            throw new RuntimeException("Token inválido o expirado");
        }

        String email = jwtTokenUtil.getEmailFromToken(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que el token de la base de datos coincida y no haya expirado
        if (!request.getToken().equals(user.getPasswordResetToken()) ||
                user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token inválido o expirado");
        }

        // Establecer la nueva contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Invalidar el token de restablecimiento
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
    }

    public void editProfile(Long userId, EditDancerProfileDto dto) {
        DancerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        profile.setCity(dto.getCity());
        profile.setDanceStyles(dto.getDanceStyles());
        profile.setLevel(dto.getLevel());
        profile.setAboutMe(dto.getAboutMe());
        profile.setAvailability(dto.getAvailability());

        // Simulación de multimedia (urls)
        List<Media> mediaList = dto.getMediaUrls().stream()
                .map(url -> {
                    Media media = new Media();
                    media.setUrl(url);
                    media.setProfile(profile);
                    return media;
                })
                .collect(Collectors.toList());

        profile.getMedia().clear();
        profile.getMedia().addAll(mediaList);

        profileRepository.save(profile);
    }

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

        DancerProfile profile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil a calificar no encontrado"));

        Rating rating = new Rating();
        rating.setRater(rater);
        rating.setProfile(profile);
        rating.setStars(dto.getStars());
        rating.setComment(dto.getComment());

        ratingRepository.save(rating);
    }

    public DancerProfileResponseDto getProfileById(Long id) {
        DancerProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        DancerProfileResponseDto dto = new DancerProfileResponseDto();
        dto.setFullName(profile.getFullName());
        dto.setAge(profile.getAge());
        dto.setCity(profile.getCity());
        dto.setDanceStyles(profile.getDanceStyles());
        dto.setLevel(profile.getLevel());
        dto.setAboutMe(profile.getAboutMe());
        dto.setAvailability(profile.getAvailability());

        List<String> mediaUrls = profile.getMedia().stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);

        // Nuevo campo: reputación promedio
        dto.setAverageRating(profile.getAverageRating());

        return dto;
    }


    public List<DancerProfileResponseDto> getAllProfiles() {
        return profileRepository.findAll().stream().map(profile -> {
            User user = profile.getUser();
            DancerProfileResponseDto dto = new DancerProfileResponseDto();
            dto.setFullName(user.getFullName());

            int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
            dto.setAge(age);

            dto.setCity(profile.getCity());
            dto.setDanceStyles(profile.getDanceStyles());
            dto.setLevel(profile.getLevel());
            dto.setAboutMe(profile.getAboutMe());
            dto.setAvailability(profile.getAvailability());

            List<String> mediaUrls = profile.getMedia().stream()
                    .map(Media::getUrl)
                    .collect(Collectors.toList());
            dto.setMediaUrls(mediaUrls);

            return dto;
        }).collect(Collectors.toList());
    }
}
