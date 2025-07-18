package org.example.bailaconmigo.Services;

import jakarta.transaction.Transactional;

import org.example.bailaconmigo.DTOs.DancerProfileResponseDto;
import org.example.bailaconmigo.DTOs.MatchResponseDto;
import org.example.bailaconmigo.Entities.DancerProfile;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Entities.Match;
import org.example.bailaconmigo.Entities.Media;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.DancerProfileRepository;
import org.example.bailaconmigo.Repositories.MatchRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private DancerProfileRepository dancerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private EmailService emailService;


    // Límite diario para BASICO
    private static final int DAILY_MATCH_LIMIT_BASIC = 3;
    private static final int DAILY_LIKE_LIMIT_BASIC = 20;

    public List<DancerProfileResponseDto> searchDancers(Long userId, String city, Set<String> styles, String level) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SubscriptionType type = user.getSubscriptionType();

        // Convertir Set<String> a Set<DanceStyle>
        Set<DanceStyle> styleEnums = styles.stream()
                .map(style -> {
                    try {
                        return DanceStyle.valueOf(style.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Estilo de baile inválido: " + style);
                    }
                })
                .collect(Collectors.toSet());

        // Obtener IDs de usuarios a los que YO di like
        Set<Long> myLikedUserIds = matchRepository.findUserIdsLikedByUser(userId);

        List<DancerProfile> results;

        // Si es usuario PRO y especifica nivel, usar búsqueda avanzada
        if (type == SubscriptionType.PRO && level != null && !level.isEmpty()) {
            Level levelEnum;
            try {
                levelEnum = Level.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Nivel inválido: " + level);
            }

            // Búsqueda PRO con nivel específico
            results = dancerProfileRepository.findAll().stream()
                    .filter(profile -> {
                        // Excluir el usuario actual
                        if (profile.getUser().getId().equals(userId)) return false;

                        // Excluir usuarios a los que YA di like
                        if (myLikedUserIds.contains(profile.getUser().getId())) return false;

                        // Filtrar por ciudad (coincidencia exacta OBLIGATORIA)
                        if (profile.getUser().getCity() == null ||
                                !city.equalsIgnoreCase(profile.getUser().getCity().getName())) return false;

                        // Filtrar por nivel (coincidencia exacta)
                        if (!levelEnum.equals(profile.getLevel())) return false;

                        // Filtrar por estilos - DEBE tener AL MENOS uno de los estilos buscados
                        boolean hasMatchingStyle = profile.getDanceStyles().stream()
                                .anyMatch(styleEnums::contains);
                        if (!hasMatchingStyle) return false;

                        return true;
                    })
                    .collect(Collectors.toList());
        } else {
            // Búsqueda básica (sin filtro de nivel)
            results = dancerProfileRepository.findAll().stream()
                    .filter(profile -> {
                        // Excluir el usuario actual
                        if (profile.getUser().getId().equals(userId)) return false;

                        // Excluir usuarios a los que YA di like
                        if (myLikedUserIds.contains(profile.getUser().getId())) return false;

                        // Filtrar por ciudad (coincidencia exacta OBLIGATORIA)
                        if (profile.getUser().getCity() == null ||
                                !city.equalsIgnoreCase(profile.getUser().getCity().getName())) return false;

                        // Filtrar por estilos - DEBE tener AL MENOS uno de los estilos buscados
                        boolean hasMatchingStyle = profile.getDanceStyles().stream()
                                .anyMatch(styleEnums::contains);
                        if (!hasMatchingStyle) return false;

                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return results.stream().map(this::toDto).collect(Collectors.toList());
    }




    @Transactional
    public MatchResponseDto likeProfile(Long likerId, Long likedUserId) {
        if (likerId.equals(likedUserId)) {
            throw new RuntimeException("No puedes likearte a ti mismo");
        }

        User liker = userRepository.findById(likerId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User likedUser = userRepository.findById(likedUserId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SubscriptionType sub = liker.getSubscriptionType();
        LocalDate today = LocalDate.now();

        long todayLikes = matchRepository.countLikesToday(likerId, today);
        if (sub == SubscriptionType.BASICO && todayLikes >= DAILY_LIKE_LIMIT_BASIC) {
            throw new RuntimeException("Límite diario de likes alcanzado");
        }

        Optional<Match> existing = matchRepository.findByLikerIdAndLikedUserId(likerId, likedUserId);
        if (existing.isPresent()) {
            throw new RuntimeException("Ya diste like a este perfil");
        }

        Optional<Match> reciprocal = matchRepository.findByLikerIdAndLikedUserId(likedUserId, likerId);
        boolean isReciprocalLike = reciprocal.isPresent() && !reciprocal.get().isMatched();

        if (isReciprocalLike && sub == SubscriptionType.BASICO) {
            long todayMatches = matchRepository.countMatchesToday(likerId, today);
            if (todayMatches >= DAILY_MATCH_LIMIT_BASIC) {
                throw new RuntimeException("Límite diario de matches alcanzado");
            }
        }

        if (isReciprocalLike) {
            Match reciprocalMatch = reciprocal.get();
            reciprocalMatch.setMatched(true);
            matchRepository.save(reciprocalMatch);
            // Notificar por email al usuario que había dado like antes
            String toEmail = likedUser.getEmail(); // este es el que dio like primero
            String fullName = likedUser.getFullName();
            String likerName = liker.getFullName();

            emailService.sendMatchNotificationEmail(toEmail, fullName, likerName);
        } else {
            Match newMatch = new Match();
            newMatch.setLiker(liker);
            newMatch.setLikedUser(likedUser);
            newMatch.setCreatedDate(today);
            newMatch.setMatched(false);
            matchRepository.save(newMatch);
        }

        return new MatchResponseDto(isReciprocalLike);
    }



    public List<DancerProfileResponseDto> getMatches(Long userId) {
        List<Match> matches = matchRepository.findMatchesByUserId(userId);
        System.out.println("Cantidad de matches encontrados: " + matches.size());

        return matches.stream()
                .map(match -> {
                    System.out.println("Match ID: " + match.getId() + " - matched: " + match.isMatched());
                    User other = match.getLiker().getId().equals(userId)
                            ? match.getLikedUser()
                            : match.getLiker();
                    return toDto(other);
                })
                .collect(Collectors.toList());
    }


    private DancerProfileResponseDto toDto(User user) {
        DancerProfile profile = dancerProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de bailarín no encontrado para el usuario con ID: " + user.getId()));

        DancerProfileResponseDto dto = new DancerProfileResponseDto();
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setAge(profile.getAge());
        dto.setCityName(user.getCity().getName());
        dto.setCountryName(user.getCountry().getName());
        dto.setDanceStyles(profile.getDanceStyles());
        dto.setLevel(profile.getLevel());
        dto.setAboutMe(profile.getAboutMe());
        dto.setAvailability(profile.getAvailability());
        List<String> mediaUrls = profile.getMedia().stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);
        dto.setAverageRating(profile.getAverageRating());
        return dto;
    }


    private DancerProfileResponseDto toDto(DancerProfile profile) {
        DancerProfileResponseDto dto = new DancerProfileResponseDto();
        dto.setUserId(profile.getUser().getId());
        dto.setFullName(profile.getUser().getFullName());
        dto.setAge(profile.getAge());
        dto.setCityName(profile.getUser().getCity().getName());
        dto.setCountryName(profile.getUser().getCountry().getName());
        dto.setDanceStyles(profile.getDanceStyles());
        dto.setLevel(profile.getLevel());
        dto.setAboutMe(profile.getAboutMe());
        dto.setAvailability(profile.getAvailability());
        List<String> mediaUrls = profile.getMedia().stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
        dto.setMediaUrls(mediaUrls);
        dto.setAverageRating(profile.getAverageRating());
        return dto;
    }
}
