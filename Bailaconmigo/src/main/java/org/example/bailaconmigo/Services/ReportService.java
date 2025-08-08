package org.example.bailaconmigo.Services;

import lombok.RequiredArgsConstructor;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.*;
import org.example.bailaconmigo.Entities.Enum.*;
import org.example.bailaconmigo.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private DancerProfileRepository dancerProfileRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private EventRatingRepository eventRatingRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    public UserStatsDto getUserStats() {
        UserStatsDto stats = new UserStatsDto();

        // Total de usuarios
        stats.setTotalUsers(userRepository.count());

        // Usuarios por género
        stats.setMaleUsers(userRepository.countByGender("Masculino"));
        stats.setFemaleUsers(userRepository.countByGender("Femenino"));
        stats.setOtherGenderUsers(userRepository.countByGender("Otro"));

        // Usuarios por tipo de suscripción
        stats.setBasicUsers(userRepository.countBySubscriptionType(SubscriptionType.BASICO));
        stats.setProUsers(userRepository.countBySubscriptionType(SubscriptionType.PRO));

        return stats;
    }

    public List<MonthlyRegistrationDto> getMonthlyRegistrations() {
        List<Object[]> results = userRepository.findMonthlyRegistrations();
        List<MonthlyRegistrationDto> monthlyStats = new ArrayList<>();

        for (Object[] result : results) {
            int year = (Integer) result[0];
            int month = (Integer) result[1];
            Long count = (Long) result[2];

            MonthlyRegistrationDto dto = new MonthlyRegistrationDto();
            dto.setYear(year);
            dto.setMonth(month);
            dto.setMonthName(Month.of(month).name());
            dto.setNewUsers(count);

            monthlyStats.add(dto);
        }

        return monthlyStats;
    }

    public List<AgeRangeDto> getAgeDistribution() {
        List<Object[]> users = userRepository.findUsersWithBirthdate();
        Map<String, Long> ageRanges = new HashMap<>();

        // Inicializar rangos
        ageRanges.put("18-25", 0L);
        ageRanges.put("26-35", 0L);
        ageRanges.put("36-45", 0L);
        ageRanges.put("46-55", 0L);
        ageRanges.put("56+", 0L);

        LocalDate now = LocalDate.now();

        for (Object[] user : users) {
            LocalDate birthdate = (LocalDate) user[0];
            if (birthdate != null) {
                int age = Period.between(birthdate, now).getYears();

                if (age >= 18 && age <= 25) {
                    ageRanges.put("18-25", ageRanges.get("18-25") + 1);
                } else if (age >= 26 && age <= 35) {
                    ageRanges.put("26-35", ageRanges.get("26-35") + 1);
                } else if (age >= 36 && age <= 45) {
                    ageRanges.put("36-45", ageRanges.get("36-45") + 1);
                } else if (age >= 46 && age <= 55) {
                    ageRanges.put("46-55", ageRanges.get("46-55") + 1);
                } else if (age > 55) {
                    ageRanges.put("56+", ageRanges.get("56+") + 1);
                }
            }
        }

        return ageRanges.entrySet().stream()
                .map(entry -> new AgeRangeDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<GeographicDto> getTopCities(int limit) {
        List<Object[]> results = userRepository.findTopCities(limit);
        List<GeographicDto> cities = new ArrayList<>();

        for (Object[] result : results) {
            String cityName = (String) result[0];
            Long userCount = (Long) result[1];
            cities.add(new GeographicDto(cityName, userCount));
        }

        return cities;
    }

    public List<GeographicDto> getTopCountries() {
        List<Object[]> results = userRepository.findUsersByCountry();
        List<GeographicDto> countries = new ArrayList<>();

        for (Object[] result : results) {
            String countryName = (String) result[0];
            Long userCount = (Long) result[1];
            countries.add(new GeographicDto(countryName, userCount));
        }

        return countries;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboard = new HashMap<>();

        // Stats básicos
        UserStatsDto userStats = getUserStats();
        dashboard.put("userStats", userStats);

        // Últimos 6 meses de registros
        List<MonthlyRegistrationDto> monthlyStats = getMonthlyRegistrations();
        dashboard.put("monthlyRegistrations", monthlyStats);

        // Top 5 ciudades
        List<GeographicDto> topCities = getTopCities(5);
        dashboard.put("topCities", topCities);

        // Distribución de edad
        List<AgeRangeDto> ageDistribution = getAgeDistribution();
        dashboard.put("ageDistribution", ageDistribution);

        // Cálculos adicionales
        double proPercentage = userStats.getTotalUsers() > 0 ?
                (userStats.getProUsers().doubleValue() / userStats.getTotalUsers() * 100) : 0;
        dashboard.put("proPercentage", Math.round(proPercentage * 100.0) / 100.0);

        return dashboard;
    }

    public MatchingReportDto generateMatchingReport() {
        MatchingReportDto report = new MatchingReportDto();

        // Métricas básicas generales
        Long totalLikes = matchRepository.countAllLikes();
        Long totalMatches = matchRepository.countAllMatches();
        Long distinctUsers = matchRepository.countDistinctLikers();

        report.setTotalLikesGiven(totalLikes);
        report.setTotalSuccessfulMatches(totalMatches);
        report.setConversionRate(calculateConversionRate(totalMatches, totalLikes));
        report.setAverageMatchesPerUser(calculateAverageMatchesPerUser(totalMatches, distinctUsers));

        // Estadísticas por período
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        LocalDate monthStart = today.minus(30, ChronoUnit.DAYS);

        report.setTodayStats(generatePeriodStats(today, today));
        report.setThisWeekStats(generatePeriodStats(weekStart, today));
        report.setThisMonthStats(generatePeriodStats(monthStart, today));

        // Top usuarios más activos (opcional - top 10)
        List<UserMatchingStatsDto> topUsers = matchRepository.findTopActiveUsers(PageRequest.of(0, 10));
        for (UserMatchingStatsDto user : topUsers) {
            if (user.getTotalLikes() != null && user.getTotalLikes() > 0) {
                user.setConversionRate(user.getTotalMatches().doubleValue() / user.getTotalLikes());
            } else {
                user.setConversionRate(0.0);
            }
        }

        report.setTopActiveUsers(topUsers);

        return report;
    }

    public MatchingReportDto generateCustomRangeReport(LocalDate startDate, LocalDate endDate) {
        MatchingReportDto report = new MatchingReportDto();

        // Métricas para el rango personalizado
        Long likes = matchRepository.countLikesByDateRange(startDate, endDate);
        Long matches = matchRepository.countMatchesByDateRange(startDate, endDate);
        Long activeUsers = matchRepository.countActiveUsersByDateRange(startDate, endDate);

        report.setTotalLikesGiven(likes);
        report.setTotalSuccessfulMatches(matches);
        report.setConversionRate(calculateConversionRate(matches, likes));
        report.setAverageMatchesPerUser(calculateAverageMatchesPerUser(matches, activeUsers));

        // Solo estadísticas del período personalizado
        MatchingPeriodStatsDto customPeriod = new MatchingPeriodStatsDto();
        customPeriod.setLikesGiven(likes);
        customPeriod.setMatchesCreated(matches);
        customPeriod.setConversionRate(calculateConversionRate(matches, likes));
        customPeriod.setActiveUsers(activeUsers);

        // Usar el período personalizado como "today stats" para mantener la estructura
        report.setTodayStats(customPeriod);

        return report;
    }

    private MatchingPeriodStatsDto generatePeriodStats(LocalDate startDate, LocalDate endDate) {
        Long likes = matchRepository.countLikesByDateRange(startDate, endDate);
        Long matches = matchRepository.countMatchesByDateRange(startDate, endDate);
        Long activeUsers = matchRepository.countActiveUsersByDateRange(startDate, endDate);

        MatchingPeriodStatsDto stats = new MatchingPeriodStatsDto();
        stats.setLikesGiven(likes);
        stats.setMatchesCreated(matches);
        stats.setConversionRate(calculateConversionRate(matches, likes));
        stats.setActiveUsers(activeUsers);

        return stats;
    }

    private Double calculateConversionRate(Long matches, Long likes) {
        if (likes == null || likes == 0) {
            return 0.0;
        }
        return (matches != null ? matches.doubleValue() : 0.0) / likes.doubleValue() * 100;
    }

    private Double calculateAverageMatchesPerUser(Long totalMatches, Long distinctUsers) {
        if (distinctUsers == null || distinctUsers == 0) {
            return 0.0;
        }
        return (totalMatches != null ? totalMatches.doubleValue() : 0.0) / distinctUsers.doubleValue();
    }

    // Método adicional para obtener estadísticas de un usuario específico
    public UserMatchingStatsDto getUserMatchingStats(Long userId) {
        // Implementar query específica para un usuario si es necesario
        List<UserMatchingStatsDto> userStats = matchRepository.findTopActiveUsers(PageRequest.of(0, Integer.MAX_VALUE));

        return userStats.stream()
                .filter(stats -> stats.getUserId().equals(userId))
                .findFirst()
                .orElse(new UserMatchingStatsDto(userId, "Usuario no encontrado", 0L, 0L, 0.0));
    }

    /**
     * Reporte de Diversidad de Baile
     * Objetivo: Demostrar la diversidad y preferencias de baile en la plataforma
     */
    public DanceDiversityReportDto generateDanceDiversityReport() {
        DanceDiversityReportDto report = new DanceDiversityReportDto();

        // Métricas básicas
        Long totalDancers = dancerProfileRepository.countActiveDancers();
        Double avgStyles = dancerProfileRepository.findAverageStylesPerDancer();

        report.setTotalActiveDancers(totalDancers);
        report.setAverageStylesPerDancer(avgStyles != null ? Math.round(avgStyles * 100.0) / 100.0 : 0.0);

        // Distribución de estilos de baile
        report.setStyleDistribution(getDanceStyleDistribution());

        // Distribución de niveles
        report.setLevelDistribution(getLevelDistribution());

        // Combinaciones más populares
        report.setTopCombinations(getTopStyleCombinations(10));

        // Estilos por región
        report.setStylesByRegion(getStylesByRegion());

        // Estadísticas por nivel y estilo
        report.setStyleLevelStats(getStyleLevelStats());

        return report;
    }

    private List<DanceStyleDistributionDto> getDanceStyleDistribution() {
        List<Object[]> results = dancerProfileRepository.findDanceStyleDistribution();
        Long totalEntries = results.stream().mapToLong(r -> (Long) r[1]).sum();

        return results.stream()
                .map(result -> {
                    DanceStyle style = (DanceStyle) result[0];
                    Long count = (Long) result[1];
                    Double percentage = totalEntries > 0 ?
                            Math.round((count.doubleValue() / totalEntries * 100) * 100.0) / 100.0 : 0.0;

                    return new DanceStyleDistributionDto(
                            style,
                            style.name(),
                            count,
                            percentage
                    );
                })
                .collect(Collectors.toList());
    }

    private List<LevelDistributionDto> getLevelDistribution() {
        List<Object[]> results = dancerProfileRepository.findLevelDistribution();
        Long totalDancers = results.stream().mapToLong(r -> (Long) r[1]).sum();

        return results.stream()
                .map(result -> {
                    Level level = (Level) result[0];
                    Long count = (Long) result[1];
                    Double percentage = totalDancers > 0 ?
                            Math.round((count.doubleValue() / totalDancers * 100) * 100.0) / 100.0 : 0.0;

                    return new LevelDistributionDto(
                            level,
                            level.name(),
                            count,
                            percentage
                    );
                })
                .collect(Collectors.toList());
    }

    private List<StyleCombinationDto> getTopStyleCombinations(int limit) {
        List<Object[]> results = dancerProfileRepository.findTopStyleCombinations(limit);
        Long totalCombinations = results.stream().mapToLong(r -> (Long) r[1]).sum();

        return results.stream()
                .map(result -> {
                    String combination = (String) result[0];
                    Long count = (Long) result[1];
                    Double percentage = totalCombinations > 0 ?
                            Math.round((count.doubleValue() / totalCombinations * 100) * 100.0) / 100.0 : 0.0;

                    return new StyleCombinationDto(combination, count, percentage);
                })
                .collect(Collectors.toList());
    }

    private List<RegionalDanceStyleDto> getStylesByRegion() {
        List<Object[]> results = dancerProfileRepository.findDanceStylesByCountry();

        return results.stream()
                .map(result -> {
                    String region = (String) result[0];
                    DanceStyle style = (DanceStyle) result[1];
                    Long count = (Long) result[2];

                    return new RegionalDanceStyleDto(
                            region,
                            style,
                            style.name(),
                            count,
                            0.0 // Se puede calcular por región si es necesario
                    );
                })
                .collect(Collectors.toList());
    }

    private List<StyleLevelStatsDto> getStyleLevelStats() {
        List<Object[]> results = dancerProfileRepository.findStyleDistributionByLevel();

        return results.stream()
                .map(result -> {
                    Level level = (Level) result[0];
                    DanceStyle style = (DanceStyle) result[1];
                    Long count = (Long) result[2];

                    return new StyleLevelStatsDto(
                            level,
                            level.name(),
                            style,
                            style.name(),
                            count,
                            0.0 // Se puede calcular porcentaje por nivel si es necesario
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Reporte Geográfico Básico Mejorado
     * Objetivo: Mostrar el alcance geográfico de la aplicación
     */
    public GeographicReportDto generateEnhancedGeographicReport() {
        GeographicReportDto report = new GeographicReportDto();

        // Métricas básicas
        Long totalUsers = userRepository.count();
        Long totalDancers = dancerProfileRepository.countActiveDancers();

        report.setTotalUsers(totalUsers);
        report.setTotalDancers(totalDancers);

        // Top ciudades con información de bailarines
        List<EnhancedGeographicDto> topCities = getEnhancedTopCities(10);
        report.setTopCities(topCities);
        report.setTotalCities(topCities.size());

        // Top países con información de bailarines
        List<EnhancedGeographicDto> topCountries = getEnhancedTopCountries();
        report.setTopCountries(topCountries);
        report.setTotalCountries(topCountries.size());

        // Rankings por estilo específico
        report.setCityStyleRankings(getCityStyleRankings());

        return report;
    }

    private List<EnhancedGeographicDto> getEnhancedTopCities(int limit) {
        List<Object[]> cityResults = userRepository.findTopCities(limit);
        Long totalUsers = userRepository.count();

        return cityResults.stream()
                .map(result -> {
                    String cityName = (String) result[0];
                    Long userCount = (Long) result[1];
                    Double percentage = totalUsers > 0 ?
                            Math.round((userCount.doubleValue() / totalUsers * 100) * 100.0) / 100.0 : 0.0;

                    // Obtener estilos populares en esta ciudad
                    List<DanceStyleDistributionDto> popularStyles = getPopularStylesInCity(cityName);

                    EnhancedGeographicDto dto = new EnhancedGeographicDto();
                    dto.setName(cityName);
                    dto.setUserCount(userCount);
                    dto.setDancerCount(0L); // Se puede implementar una query específica si se necesita
                    dto.setPercentage(percentage);
                    dto.setPopularStyles(popularStyles);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<EnhancedGeographicDto> getEnhancedTopCountries() {
        List<Object[]> countryResults = userRepository.findUsersByCountry();
        Long totalUsers = userRepository.count();

        return countryResults.stream()
                .map(result -> {
                    String countryName = (String) result[0];
                    Long userCount = (Long) result[1];
                    Double percentage = totalUsers > 0 ?
                            Math.round((userCount.doubleValue() / totalUsers * 100) * 100.0) / 100.0 : 0.0;

                    EnhancedGeographicDto dto = new EnhancedGeographicDto();
                    dto.setName(countryName);
                    dto.setUserCount(userCount);
                    dto.setDancerCount(0L);
                    dto.setPercentage(percentage);
                    dto.setPopularStyles(new ArrayList<>());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<DanceStyleDistributionDto> getPopularStylesInCity(String cityName) {
        // Esta es una implementación básica, se puede mejorar con una query más específica
        return new ArrayList<>();
    }

    private List<CityStyleRankingDto> getCityStyleRankings() {
        List<CityStyleRankingDto> rankings = new ArrayList<>();

        // Para cada estilo de baile, obtener las top ciudades
        for (DanceStyle style : DanceStyle.values()) {
            List<Object[]> cityRanking = dancerProfileRepository.findCitiesByDanceStyle(style);

            List<GeographicDto> topCitiesForStyle = cityRanking.stream()
                    .limit(5) // Top 5 ciudades por estilo
                    .map(result -> new GeographicDto((String) result[0], (Long) result[1]))
                    .collect(Collectors.toList());

            if (!topCitiesForStyle.isEmpty()) {
                rankings.add(new CityStyleRankingDto(style, style.name(), topCitiesForStyle));
            }
        }

        return rankings;
    }

    /**
     * Dashboard completo con métricas de baile y geográficas
     */
    public Map<String, Object> getCompleteDashboard() {
        Map<String, Object> dashboard = getDashboardStats(); // Mantener stats existentes

        // Agregar reportes de baile
        DanceDiversityReportDto danceReport = generateDanceDiversityReport();
        dashboard.put("danceDiversity", danceReport);

        // Agregar reportes geográficos mejorados
        GeographicReportDto geoReport = generateEnhancedGeographicReport();
        dashboard.put("geographicReport", geoReport);

        // Métricas rápidas adicionales
        dashboard.put("mostPopularStyle", getMostPopularStyle());
        dashboard.put("mostActiveDanceCity", getMostActiveDanceCity());

        return dashboard;
    }

    private String getMostPopularStyle() {
        List<DanceStyleDistributionDto> styles = getDanceStyleDistribution();
        return styles.isEmpty() ? "N/A" : styles.get(0).getStyleName();
    }

    private String getMostActiveDanceCity() {
        List<Object[]> cities = dancerProfileRepository.findDanceStylesByCity();
        if (cities.isEmpty()) return "N/A";

        Map<String, Long> cityCount = new HashMap<>();
        for (Object[] result : cities) {
            String city = (String) result[0];
            Long count = (Long) result[2];
            cityCount.merge(city, count, Long::sum);
        }

        return cityCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }


    /**
     * Reporte general de eventos
     */
    public EventGeneralReportDto getEventGeneralReport() {
        List<Event> allEvents = eventRepository.findAll();
        List<EventRegistration> allRegistrations = registrationRepository.findAll();

        EventGeneralReportDto report = new EventGeneralReportDto();
        report.setGeneratedAt(LocalDateTime.now());

        // Estadísticas generales
        report.setTotalEvents(allEvents.size());
        report.setActiveEvents((int) allEvents.stream().filter(e -> e.getStatus() == EventStatus.ACTIVO).count());
        report.setCancelledEvents((int) allEvents.stream().filter(e -> e.getStatus() == EventStatus.CANCELADO).count());

        report.setTotalRegistrations(allRegistrations.size());
        report.setConfirmedRegistrations((int) allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO).count());
        report.setPendingRegistrations((int) allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDIENTE).count());
        report.setCancelledRegistrations((int) allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CANCELADO).count());

        // Ingresos totales
        BigDecimal totalRevenue = allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO && r.getPaidAmount() != null)
                .map(EventRegistration::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(totalRevenue);

        // Promedio de inscripciones por evento
        double avgRegistrations = allEvents.isEmpty() ? 0 :
                (double) allRegistrations.size() / allEvents.size();
        report.setAverageRegistrationsPerEvent(avgRegistrations);

        // Eventos por tipo
        Map<EventType, Long> eventsByType = allEvents.stream()
                .collect(Collectors.groupingBy(Event::getEventType, Collectors.counting()));
        report.setEventsByType(eventsByType);

        // Estilos de baile más populares
        Map<DanceStyle, Long> stylePopularity = allEvents.stream()
                .flatMap(e -> e.getDanceStyles().stream())
                .collect(Collectors.groupingBy(style -> style, Collectors.counting()));
        report.setDanceStylePopularity(stylePopularity);

        // NUEVO: promedio de asistencia
        long totalConfirmed = allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                .count();

        long totalAttended = allRegistrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO && Boolean.TRUE.equals(r.getAttended()))
                .count();

        double attendanceRate = totalConfirmed == 0 ? 0.0 : (totalAttended * 100.0) / totalConfirmed;
        report.setAverageAttendanceRate(attendanceRate);

        return report;
    }


    /**
     * Reporte de eventos por organizador
     */
    public List<OrganizerEventReportDto> getOrganizerEventReports() {
        List<OrganizerProfile> organizers = organizerProfileRepository.findAll();

        return organizers.stream().map(organizer -> {
            List<Event> organizerEvents = eventRepository.findByOrganizer(organizer);

            OrganizerEventReportDto report = new OrganizerEventReportDto();
            report.setOrganizerId(organizer.getId());
            report.setOrganizerName(organizer.getOrganizationName());

            report.setTotalEvents(organizerEvents.size());
            report.setActiveEvents((int) organizerEvents.stream()
                    .filter(e -> e.getStatus() == EventStatus.ACTIVO).count());
            report.setCancelledEvents((int) organizerEvents.stream()
                    .filter(e -> e.getStatus() == EventStatus.CANCELADO).count());

            // Calcular inscripciones totales para este organizador
            int totalRegistrations = organizerEvents.stream()
                    .mapToInt(e -> e.getRegistrations().size()).sum();
            report.setTotalRegistrations(totalRegistrations);

            // Calcular ingresos del organizador
            BigDecimal organizerRevenue = organizerEvents.stream()
                    .flatMap(e -> e.getRegistrations().stream())
                    .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                    .filter(r -> r.getOrganizerAmount() != null)
                    .map(EventRegistration::getOrganizerAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            report.setTotalRevenue(organizerRevenue);

            // Rating promedio
            double avgRating = organizerEvents.stream()
                    .mapToDouble(Event::getAverageRating)
                    .average().orElse(0.0);
            report.setAverageRating(avgRating);

            return report;
        }).collect(Collectors.toList());
    }

    /**
     * Reporte detallado de un evento específico
     */
    public EventDetailReportDto getEventDetailReport(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        EventDetailReportDto report = new EventDetailReportDto();
        report.setEventId(event.getId());
        report.setEventName(event.getName());
        report.setEventType(event.getEventType());
        report.setEventStatus(event.getStatus());
        report.setEventDate(event.getDateTime());
        report.setOrganizerName(event.getOrganizer().getOrganizationName());
        report.setCityName(event.getCityName());
        report.setCountryName(event.getCountryName());

        report.setCapacity(event.getCapacity());
        report.setPrice(event.getPrice());
        report.setDanceStyles(new ArrayList<>(event.getDanceStyles()));

        // Estadísticas de inscripciones
        List<EventRegistration> registrations = event.getRegistrations();
        report.setTotalRegistrations(registrations.size());
        report.setConfirmedRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO).count());
        report.setPendingRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDIENTE).count());
        report.setCancelledRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CANCELADO).count());

        report.setAvailableCapacity(event.getAvailableCapacity());
        report.setOccupancyRate(event.getCapacity() > 0 ?
                (double) report.getConfirmedRegistrations() / event.getCapacity() * 100 : 0);

        // Ingresos del evento
        BigDecimal eventRevenue = registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                .filter(r -> r.getPaidAmount() != null)
                .map(EventRegistration::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(eventRevenue);

        // Rating
        report.setAverageRating(event.getAverageRating());
        report.setTotalRatings(event.getRatings().size());

        // Lista de participantes confirmados
        List<ParticipantInfoDto> participants = registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                .map(r -> {
                    ParticipantInfoDto participant = new ParticipantInfoDto();
                    participant.setDancerId(r.getDancer().getId());
                    participant.setDancerName(r.getDancer().getFullName());
                    participant.setRegistrationDate(r.getRegistrationDate());
                    participant.setPaidAmount(r.getPaidAmount());
                    return participant;
                })
                .collect(Collectors.toList());
        report.setParticipants(participants);

        return report;
    }

    /**
     * Reporte de inscripciones por período
     */
    public RegistrationReportDto getRegistrationReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<EventRegistration> registrations = registrationRepository.findAll().stream()
                .filter(r -> r.getRegistrationDate().isAfter(startDateTime) &&
                        r.getRegistrationDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        RegistrationReportDto report = new RegistrationReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        report.setTotalRegistrations(registrations.size());
        report.setConfirmedRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO).count());
        report.setPendingRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.PENDIENTE).count());
        report.setCancelledRegistrations((int) registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CANCELADO).count());

        // Ingresos del período
        BigDecimal periodRevenue = registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                .filter(r -> r.getPaidAmount() != null)
                .map(EventRegistration::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setPeriodRevenue(periodRevenue);

        // Inscripciones por día
        Map<LocalDate, Long> registrationsByDay = registrations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRegistrationDate().toLocalDate(),
                        Collectors.counting()
                ));
        report.setRegistrationsByDay(registrationsByDay);

        return report;
    }

    /**
     * Reporte de eventos por ubicación
     */
    public List<LocationEventReportDto> getLocationEventReports() {
        List<Event> allEvents = eventRepository.findAll();

        // Agrupar por país y ciudad
        Map<String, Map<String, List<Event>>> eventsByLocation = allEvents.stream()
                .filter(e -> e.getCountryName() != null && e.getCityName() != null)
                .collect(Collectors.groupingBy(
                        Event::getCountryName,
                        Collectors.groupingBy(Event::getCityName)
                ));

        List<LocationEventReportDto> reports = new ArrayList<>();

        eventsByLocation.forEach((country, cities) -> {
            cities.forEach((city, events) -> {
                LocationEventReportDto report = new LocationEventReportDto();
                report.setCountryName(country);
                report.setCityName(city);
                report.setTotalEvents(events.size());
                report.setActiveEvents((int) events.stream()
                        .filter(e -> e.getStatus() == EventStatus.ACTIVO).count());

                // Total de inscripciones en esta ubicación
                int totalRegistrations = events.stream()
                        .mapToInt(e -> e.getRegistrations().size()).sum();
                report.setTotalRegistrations(totalRegistrations);

                // Ingresos por ubicación
                BigDecimal locationRevenue = events.stream()
                        .flatMap(e -> e.getRegistrations().stream())
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                        .filter(r -> r.getPaidAmount() != null)
                        .map(EventRegistration::getPaidAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                report.setTotalRevenue(locationRevenue);

                reports.add(report);
            });
        });

        return reports.stream()
                .sorted((a, b) -> Integer.compare(b.getTotalEvents(), a.getTotalEvents()))
                .collect(Collectors.toList());
    }

    /**
     * Reporte de rendimiento financiero
     */
    public FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<EventRegistration> registrations = registrationRepository.findAll().stream()
                .filter(r -> r.getRegistrationDate().isAfter(startDateTime) &&
                        r.getRegistrationDate().isBefore(endDateTime))
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMADO)
                .collect(Collectors.toList());

        FinancialReportDto report = new FinancialReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // Ingresos totales
        BigDecimal totalRevenue = registrations.stream()
                .filter(r -> r.getPaidAmount() != null)
                .map(EventRegistration::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(totalRevenue);

        // Comisiones de la app
        BigDecimal totalAppFees = registrations.stream()
                .filter(r -> r.getAppFee() != null)
                .map(EventRegistration::getAppFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalAppFees(totalAppFees);

        // Dinero para organizadores
        BigDecimal totalOrganizerAmount = registrations.stream()
                .filter(r -> r.getOrganizerAmount() != null)
                .map(EventRegistration::getOrganizerAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalOrganizerAmount(totalOrganizerAmount);

        // Transacciones por método de pago
        Map<String, Long> paymentMethods = registrations.stream()
                .filter(r -> r.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        EventRegistration::getPaymentMethod,
                        Collectors.counting()
                ));
        report.setTransactionsByPaymentMethod(paymentMethods);

        // Ingresos por tipo de evento
        Map<EventType, BigDecimal> revenueByEventType = registrations.stream()
                .filter(r -> r.getPaidAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getEvent().getEventType(),
                        Collectors.reducing(BigDecimal.ZERO,
                                EventRegistration::getPaidAmount,
                                BigDecimal::add)
                ));
        report.setRevenueByEventType(revenueByEventType);

        return report;
    }

    /**
     * Reporte de eventos próximos
     */
    public List<UpcomingEventReportDto> getUpcomingEventsReport(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);

        List<Event> upcomingEvents = eventRepository.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.ACTIVO)
                .filter(e -> e.getDateTime().isAfter(now) && e.getDateTime().isBefore(futureDate))
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());

        return upcomingEvents.stream().map(event -> {
            UpcomingEventReportDto report = new UpcomingEventReportDto();
            report.setEventId(event.getId());
            report.setEventName(event.getName());
            report.setEventDate(event.getDateTime());
            report.setOrganizerName(event.getOrganizer().getOrganizationName());
            report.setCityName(event.getCityName());
            report.setCapacity(event.getCapacity());
            report.setCurrentRegistrations(event.getRegistrations().size());
            report.setAvailableSpots(event.getAvailableCapacity());
            report.setOccupancyRate(event.getCapacity() > 0 ?
                    (double) event.getRegistrations().size() / event.getCapacity() * 100 : 0);

            return report;
        }).collect(Collectors.toList());
    }


    /**
     * Reporte general de ratings de eventos
     */
    public EventRatingsReportDto getEventRatingsReport() {
        List<EventRating> allEventRatings = eventRatingRepository.findAll();

        EventRatingsReportDto report = new EventRatingsReportDto();
        report.setGeneratedAt(LocalDateTime.now());

        // Estadísticas básicas
        report.setTotalEventRatings(allEventRatings.size());

        if (!allEventRatings.isEmpty()) {
            // Promedio general de ratings de eventos
            double avgRating = allEventRatings.stream()
                    .mapToInt(EventRating::getStars)
                    .average()
                    .orElse(0.0);
            report.setAverageEventRating(Math.round(avgRating * 100.0) / 100.0);

            // Distribución de estrellas
            Map<Integer, Long> starsDistribution = allEventRatings.stream()
                    .collect(Collectors.groupingBy(EventRating::getStars, Collectors.counting()));
            report.setStarsDistribution(starsDistribution);

            // Porcentaje de ratings con comentarios
            long ratingsWithComments = allEventRatings.stream()
                    .filter(r -> r.getComment() != null && !r.getComment().trim().isEmpty())
                    .count();
            double commentPercentage = (ratingsWithComments * 100.0) / allEventRatings.size();
            report.setCommentPercentage(Math.round(commentPercentage * 100.0) / 100.0);
        } else {
            report.setAverageEventRating(0.0);
            report.setStarsDistribution(new HashMap<>());
            report.setCommentPercentage(0.0);
        }

        // Top eventos mejor calificados
        report.setTopRatedEvents(getTopRatedEvents(10));

        // Eventos con más ratings
        report.setMostRatedEvents(getMostRatedEvents(10));

        return report;
    }

    /**
     * Reporte general de ratings de perfiles
     */
    public ProfileRatingsReportDto getProfileRatingsReport() {
        List<Rating> allProfileRatings = ratingRepository.findAll();

        ProfileRatingsReportDto report = new ProfileRatingsReportDto();
        report.setGeneratedAt(LocalDateTime.now());

        // Estadísticas básicas
        report.setTotalProfileRatings(allProfileRatings.size());

        if (!allProfileRatings.isEmpty()) {
            // Promedio general de ratings de perfiles
            double avgRating = allProfileRatings.stream()
                    .mapToInt(Rating::getStars)
                    .average()
                    .orElse(0.0);
            report.setAverageProfileRating(Math.round(avgRating * 100.0) / 100.0);

            // Distribución de estrellas
            Map<Integer, Long> starsDistribution = allProfileRatings.stream()
                    .collect(Collectors.groupingBy(Rating::getStars, Collectors.counting()));
            report.setStarsDistribution(starsDistribution);

            // Porcentaje de ratings con comentarios
            long ratingsWithComments = allProfileRatings.stream()
                    .filter(r -> r.getComment() != null && !r.getComment().trim().isEmpty())
                    .count();
            double commentPercentage = (ratingsWithComments * 100.0) / allProfileRatings.size();
            report.setCommentPercentage(Math.round(commentPercentage * 100.0) / 100.0);
        } else {
            report.setAverageProfileRating(0.0);
            report.setStarsDistribution(new HashMap<>());
            report.setCommentPercentage(0.0);
        }

        // Top perfiles mejor calificados
        report.setTopRatedProfiles(getTopRatedProfiles(10));

        // Perfiles con más ratings
        report.setMostRatedProfiles(getMostRatedProfiles(10));

        return report;
    }

    /**
     * Reporte de satisfacción por tipo de evento
     */
    public List<EventTypeSatisfactionDto> getEventTypeSatisfactionReport() {
        List<Event> allEvents = eventRepository.findAll();

        Map<EventType, List<Double>> ratingsByType = new HashMap<>();

        // Agrupar ratings por tipo de evento
        for (Event event : allEvents) {
            if (!event.getRatings().isEmpty()) {
                double avgRating = event.getAverageRating();
                ratingsByType.computeIfAbsent(event.getEventType(), k -> new ArrayList<>())
                        .add(avgRating);
            }
        }

        return ratingsByType.entrySet().stream()
                .map(entry -> {
                    EventType type = entry.getKey();
                    List<Double> ratings = entry.getValue();

                    double avgSatisfaction = ratings.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    EventTypeSatisfactionDto dto = new EventTypeSatisfactionDto();
                    dto.setEventType(type);
                    dto.setEventTypeName(type.name());
                    dto.setAverageRating(Math.round(avgSatisfaction * 100.0) / 100.0);
                    dto.setTotalRatedEvents(ratings.size());
                    dto.setTotalEvents((int) allEvents.stream()
                            .filter(e -> e.getEventType() == type)
                            .count());

                    return dto;
                })
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .collect(Collectors.toList());
    }

    /**
     * Reporte de satisfacción por estilo de baile
     */
    public List<DanceStyleSatisfactionDto> getDanceStyleSatisfactionReport() {
        List<Event> allEvents = eventRepository.findAll();

        Map<DanceStyle, List<Double>> ratingsByStyle = new HashMap<>();

        // Agrupar ratings por estilo de baile
        for (Event event : allEvents) {
            if (!event.getRatings().isEmpty() && !event.getDanceStyles().isEmpty()) {
                double avgRating = event.getAverageRating();
                for (DanceStyle style : event.getDanceStyles()) {
                    ratingsByStyle.computeIfAbsent(style, k -> new ArrayList<>())
                            .add(avgRating);
                }
            }
        }

        return ratingsByStyle.entrySet().stream()
                .map(entry -> {
                    DanceStyle style = entry.getKey();
                    List<Double> ratings = entry.getValue();

                    double avgSatisfaction = ratings.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    DanceStyleSatisfactionDto dto = new DanceStyleSatisfactionDto();
                    dto.setDanceStyle(style);
                    dto.setStyleName(style.name());
                    dto.setAverageRating(Math.round(avgSatisfaction * 100.0) / 100.0);
                    dto.setTotalRatedEvents(ratings.size());

                    return dto;
                })
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .collect(Collectors.toList());
    }

    /**
     * Reporte detallado de un evento específico incluyendo ratings
     */
    public EventRatingDetailDto getEventRatingDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        EventRatingDetailDto detail = new EventRatingDetailDto();
        detail.setEventId(event.getId());
        detail.setEventName(event.getName());
        detail.setEventType(event.getEventType());
        detail.setEventDate(event.getDateTime());

        List<EventRating> ratings = event.getRatings();

        if (!ratings.isEmpty()) {
            detail.setTotalRatings(ratings.size());
            detail.setAverageRating(event.getAverageRating());

            // Distribución de estrellas
            Map<Integer, Long> distribution = ratings.stream()
                    .collect(Collectors.groupingBy(EventRating::getStars, Collectors.counting()));
            detail.setStarsDistribution(distribution);

            // Comentarios más recientes
            List<RatingCommentDto> recentComments = ratings.stream()
                    .filter(r -> r.getComment() != null && !r.getComment().trim().isEmpty())
                    .sorted((a, b) -> b.getId().compareTo(a.getId())) // Asumiendo que ID mayor = más reciente
                    .limit(10)
                    .map(r -> new RatingCommentDto(
                            r.getRater().getFullName(),
                            r.getStars(),
                            r.getComment()
                    ))
                    .collect(Collectors.toList());
            detail.setRecentComments(recentComments);
        } else {
            detail.setTotalRatings(0);
            detail.setAverageRating(0.0);
            detail.setStarsDistribution(new HashMap<>());
            detail.setRecentComments(new ArrayList<>());
        }

        return detail;
    }

    /**
     * Reporte detallado de un perfil específico incluyendo ratings
     */
    public ProfileRatingDetailDto getProfileRatingDetail(Long profileId) {
        DancerProfile profile = dancerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        ProfileRatingDetailDto detail = new ProfileRatingDetailDto();
        detail.setProfileId(profile.getId());
        detail.setProfileName(profile.getFullName());
        detail.setDanceStyles(new ArrayList<>(profile.getDanceStyles()));
        detail.setLevel(profile.getLevel());

        List<Rating> ratings = profile.getRatings();

        if (!ratings.isEmpty()) {
            detail.setTotalRatings(ratings.size());
            detail.setAverageRating(profile.getAverageRating());

            // Distribución de estrellas
            Map<Integer, Long> distribution = ratings.stream()
                    .collect(Collectors.groupingBy(Rating::getStars, Collectors.counting()));
            detail.setStarsDistribution(distribution);

            // Comentarios más recientes
            List<RatingCommentDto> recentComments = ratings.stream()
                    .filter(r -> r.getComment() != null && !r.getComment().trim().isEmpty())
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(10)
                    .map(r -> new RatingCommentDto(
                            r.getRater().getFullName(),
                            r.getStars(),
                            r.getComment()
                    ))
                    .collect(Collectors.toList());
            detail.setRecentComments(recentComments);
        } else {
            detail.setTotalRatings(0);
            detail.setAverageRating(0.0);
            detail.setStarsDistribution(new HashMap<>());
            detail.setRecentComments(new ArrayList<>());
        }

        return detail;
    }

    /**
     * Métodos auxiliares para obtener tops
     */
    private List<TopRatedEventDto> getTopRatedEvents(int limit) {
        return eventRepository.findAll().stream()
                .filter(e -> !e.getRatings().isEmpty())
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .limit(limit)
                .map(e -> new TopRatedEventDto(
                        e.getId(),
                        e.getName(),
                        e.getEventType(),
                        e.getAverageRating(),
                        e.getRatings().size()
                ))
                .collect(Collectors.toList());
    }

    private List<TopRatedEventDto> getMostRatedEvents(int limit) {
        return eventRepository.findAll().stream()
                .filter(e -> !e.getRatings().isEmpty())
                .sorted((a, b) -> Integer.compare(b.getRatings().size(), a.getRatings().size()))
                .limit(limit)
                .map(e -> new TopRatedEventDto(
                        e.getId(),
                        e.getName(),
                        e.getEventType(),
                        e.getAverageRating(),
                        e.getRatings().size()
                ))
                .collect(Collectors.toList());
    }

    private List<TopRatedProfileDto> getTopRatedProfiles(int limit) {
        return dancerProfileRepository.findAll().stream()
                .filter(p -> !p.getRatings().isEmpty())
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .limit(limit)
                .map(p -> new TopRatedProfileDto(
                        p.getId(),
                        p.getFullName(),
                        p.getLevel(),
                        p.getAverageRating(),
                        p.getRatings().size(),
                        new ArrayList<>(p.getDanceStyles())
                ))
                .collect(Collectors.toList());
    }

    private List<TopRatedProfileDto> getMostRatedProfiles(int limit) {
        return dancerProfileRepository.findAll().stream()
                .filter(p -> !p.getRatings().isEmpty())
                .sorted((a, b) -> Integer.compare(b.getRatings().size(), a.getRatings().size()))
                .limit(limit)
                .map(p -> new TopRatedProfileDto(
                        p.getId(),
                        p.getFullName(),
                        p.getLevel(),
                        p.getAverageRating(),
                        p.getRatings().size(),
                        new ArrayList<>(p.getDanceStyles())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Reporte de organizadores por rating promedio
     */
    public List<OrganizerRatingReportDto> getOrganizerRatingReport() {
        List<OrganizerProfile> organizers = organizerProfileRepository.findAll();

        return organizers.stream()
                .map(organizer -> {
                    List<Event> organizerEvents = eventRepository.findByOrganizer(organizer);

                    // Calcular rating promedio de todos los eventos del organizador
                    List<Double> eventRatings = organizerEvents.stream()
                            .filter(e -> !e.getRatings().isEmpty())
                            .map(Event::getAverageRating)
                            .collect(Collectors.toList());

                    double avgRating = eventRatings.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    int totalRatings = organizerEvents.stream()
                            .mapToInt(e -> e.getRatings().size())
                            .sum();

                    OrganizerRatingReportDto report = new OrganizerRatingReportDto();
                    report.setOrganizerId(organizer.getId());
                    report.setOrganizerName(organizer.getOrganizationName());
                    report.setAverageRating(Math.round(avgRating * 100.0) / 100.0);
                    report.setTotalRatings(totalRatings);
                    report.setTotalEvents(organizerEvents.size());
                    report.setRatedEvents(eventRatings.size());

                    return report;
                })
                .filter(r -> r.getTotalRatings() > 0) // Solo organizadores con ratings
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .collect(Collectors.toList());
    }

    /**
     * Actualizar el dashboard completo para incluir métricas de rating
     */
    public Map<String, Object> getCompleteRatingDashboard() {
        Map<String, Object> dashboard = getCompleteDashboard();

        // Agregar reportes de ratings
        EventRatingsReportDto eventRatings = getEventRatingsReport();
        ProfileRatingsReportDto profileRatings = getProfileRatingsReport();
        List<EventTypeSatisfactionDto> eventTypeSatisfaction = getEventTypeSatisfactionReport();
        List<DanceStyleSatisfactionDto> stylesSatisfaction = getDanceStyleSatisfactionReport();
        List<OrganizerRatingReportDto> organizerRatings = getOrganizerRatingReport();

        dashboard.put("eventRatings", eventRatings);
        dashboard.put("profileRatings", profileRatings);
        dashboard.put("eventTypeSatisfaction", eventTypeSatisfaction);
        dashboard.put("stylesSatisfaction", stylesSatisfaction);
        dashboard.put("organizerRatings", organizerRatings);

        // Métricas rápidas de rating
        dashboard.put("overallEventSatisfaction", eventRatings.getAverageEventRating());
        dashboard.put("overallProfileSatisfaction", profileRatings.getAverageProfileRating());
        dashboard.put("bestRatedEventType", eventTypeSatisfaction.isEmpty() ? "N/A" : eventTypeSatisfaction.get(0).getEventTypeName());
        dashboard.put("bestRatedStyle", stylesSatisfaction.isEmpty() ? "N/A" : stylesSatisfaction.get(0).getStyleName());

        return dashboard;
    }
}
