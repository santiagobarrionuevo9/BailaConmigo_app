package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.*;
import org.example.bailaconmigo.Services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")

public class ReportController {

    @Autowired
    private ReportService userReportService;

    // Endpoint principal - Dashboard completo
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getUserDashboard() {
        try {
            Map<String, Object> dashboard = userReportService.getDashboardStats();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Estadísticas básicas de usuarios
    @GetMapping("/stats")
    public ResponseEntity<UserStatsDto> getUserStats() {
        try {
            UserStatsDto stats = userReportService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Registros mensuales
    @GetMapping("/monthly-registrations")
    public ResponseEntity<List<MonthlyRegistrationDto>> getMonthlyRegistrations() {
        try {
            List<MonthlyRegistrationDto> registrations = userReportService.getMonthlyRegistrations();
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Distribución por edad
    @GetMapping("/age-distribution")
    public ResponseEntity<List<AgeRangeDto>> getAgeDistribution() {
        try {
            List<AgeRangeDto> ageDistribution = userReportService.getAgeDistribution();
            return ResponseEntity.ok(ageDistribution);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Top ciudades
    @GetMapping("/top-cities")
    public ResponseEntity<List<GeographicDto>> getTopCities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<GeographicDto> cities = userReportService.getTopCities(limit);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Usuarios por país
    @GetMapping("/by-country")
    public ResponseEntity<List<GeographicDto>> getUsersByCountry() {
        try {
            List<GeographicDto> countries = userReportService.getTopCountries();
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para exportar datos (CSV format)
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportUserStatsCSV() {
        try {
            UserStatsDto stats = userReportService.getUserStats();
            StringBuilder csv = new StringBuilder();
            csv.append("Metric,Value\n");
            csv.append("Total Users,").append(stats.getTotalUsers()).append("\n");
            csv.append("Male Users,").append(stats.getMaleUsers()).append("\n");
            csv.append("Female Users,").append(stats.getFemaleUsers()).append("\n");
            csv.append("Other Gender,").append(stats.getOtherGenderUsers()).append("\n");
            csv.append("Basic Users,").append(stats.getBasicUsers()).append("\n");
            csv.append("Pro Users,").append(stats.getProUsers()).append("\n");

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=user_stats.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Genera un reporte completo de actividad de matching
     * Incluye métricas generales y estadísticas de hoy, esta semana y este mes
     */
    @GetMapping("/activity")
    public ResponseEntity<MatchingReportDto> getMatchingActivityReport() {
        try {
            MatchingReportDto report = userReportService.generateMatchingReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Genera un reporte de matching para un rango de fechas personalizado
     * @param startDate Fecha de inicio (formato: YYYY-MM-DD)
     * @param endDate Fecha de fin (formato: YYYY-MM-DD)
     */
    @GetMapping("/activity/range")
    public ResponseEntity<MatchingReportDto> getMatchingReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }

            MatchingReportDto report = userReportService.generateCustomRangeReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene las estadísticas de matching de un usuario específico
     * @param userId ID del usuario
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<UserMatchingStatsDto> getUserMatchingStats(@PathVariable Long userId) {
        try {
            UserMatchingStatsDto stats = userReportService.getUserMatchingStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint simple para obtener solo las métricas básicas (útil para dashboards)
     */
    @GetMapping("/metrics")
    public ResponseEntity<BasicMetricsDto> getBasicMetrics() {
        try {
            MatchingReportDto fullReport = userReportService.generateMatchingReport();

            BasicMetricsDto metrics = new BasicMetricsDto();
            metrics.setTotalLikes(fullReport.getTotalLikesGiven());
            metrics.setTotalMatches(fullReport.getTotalSuccessfulMatches());
            metrics.setConversionRate(fullReport.getConversionRate());
            metrics.setAverageMatchesPerUser(fullReport.getAverageMatchesPerUser());

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO para métricas básicas
    public static class BasicMetricsDto {
        private Long totalLikes;
        private Long totalMatches;
        private Double conversionRate;
        private Double averageMatchesPerUser;

        // Getters y Setters
        public Long getTotalLikes() { return totalLikes; }
        public void setTotalLikes(Long totalLikes) { this.totalLikes = totalLikes; }

        public Long getTotalMatches() { return totalMatches; }
        public void setTotalMatches(Long totalMatches) { this.totalMatches = totalMatches; }

        public Double getConversionRate() { return conversionRate; }
        public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }

        public Double getAverageMatchesPerUser() { return averageMatchesPerUser; }
        public void setAverageMatchesPerUser(Double averageMatchesPerUser) { this.averageMatchesPerUser = averageMatchesPerUser; }
    }
}
