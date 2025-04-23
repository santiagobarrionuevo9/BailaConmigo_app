package org.example.bailaconmigo.Services;

import org.example.bailaconmigo.DTOs.DancerProfileResponseDto;
import org.example.bailaconmigo.DTOs.EditDancerProfileDto;
import org.example.bailaconmigo.DTOs.RegisterRequestDto;
import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Entities.Enum.Role;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Entities.Media;
import org.example.bailaconmigo.Repositories.DancerProfileRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
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
