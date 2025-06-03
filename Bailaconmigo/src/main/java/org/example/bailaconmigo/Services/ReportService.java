package org.example.bailaconmigo.Services;

import lombok.RequiredArgsConstructor;
import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Entities.Enum.SubscriptionType;
import org.example.bailaconmigo.Repositories.DancerProfileRepository;
import org.example.bailaconmigo.Repositories.MatchRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

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

}
