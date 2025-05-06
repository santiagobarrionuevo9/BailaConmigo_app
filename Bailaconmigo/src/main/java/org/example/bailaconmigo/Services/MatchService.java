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

    // Límite diario para BASICO
    private static final int DAILY_MATCH_LIMIT_BASIC = 3;
    private static final int DAILY_LIKE_LIMIT_BASIC = 20;

    public List<DancerProfileResponseDto> searchDancers(Long userId, String city, Set<String> styles, String level, String availability) {
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

        // Si es usuario PRO, usar búsqueda avanzada
        if (type == SubscriptionType.PRO && level != null && availability != null) {
            Level levelEnum;
            try {
                levelEnum = Level.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Nivel inválido: " + level);
            }

            List<DancerProfile> advancedResults = dancerProfileRepository
                    .findByCityAndDanceStylesInAndLevelAndAvailability(city, styleEnums, levelEnum, availability);

            return advancedResults.stream().map(this::toDto).collect(Collectors.toList());
        }

        // Si es básico o no hay filtros extra, usar búsqueda simple
        List<DancerProfile> basicResults = dancerProfileRepository
                .findByCityAndDanceStylesIn(city, styleEnums);

        return basicResults.stream().map(this::toDto).collect(Collectors.toList());
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
        dto.setFullName(user.getFullName());
        dto.setAge(profile.getAge());
        dto.setCity(user.getCity());
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
        dto.setFullName(profile.getUser().getFullName());
        dto.setAge(profile.getAge());
        dto.setCity(profile.getUser().getCity());
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
