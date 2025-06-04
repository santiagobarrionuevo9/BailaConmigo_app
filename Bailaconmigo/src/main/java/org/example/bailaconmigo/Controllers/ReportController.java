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

    // === DASHBOARD Y MÉTRICAS GENERALES ===

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getUserDashboard() {
        try {
            return ResponseEntity.ok(userReportService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/complete")
    public ResponseEntity<Map<String, Object>> getCompleteDashboard() {
        return ResponseEntity.ok(userReportService.getCompleteDashboard());
    }

    @GetMapping("/metrics")
    public ResponseEntity<BasicMetricsDto> getBasicMetrics() {
        try {
            MatchingReportDto report = userReportService.generateMatchingReport();
            BasicMetricsDto metrics = new BasicMetricsDto();
            metrics.setTotalLikes(report.getTotalLikesGiven());
            metrics.setTotalMatches(report.getTotalSuccessfulMatches());
            metrics.setConversionRate(report.getConversionRate());
            metrics.setAverageMatchesPerUser(report.getAverageMatchesPerUser());
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public static class BasicMetricsDto {
        private Long totalLikes;
        private Long totalMatches;
        private Double conversionRate;
        private Double averageMatchesPerUser;
        public Long getTotalLikes() { return totalLikes; }
        public void setTotalLikes(Long totalLikes) { this.totalLikes = totalLikes; }
        public Long getTotalMatches() { return totalMatches; }
        public void setTotalMatches(Long totalMatches) { this.totalMatches = totalMatches; }
        public Double getConversionRate() { return conversionRate; }
        public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
        public Double getAverageMatchesPerUser() { return averageMatchesPerUser; }
        public void setAverageMatchesPerUser(Double averageMatchesPerUser) { this.averageMatchesPerUser = averageMatchesPerUser; }
    }

    // === ACTIVIDAD DE MATCHING ===

    @GetMapping("/activity")
    public ResponseEntity<MatchingReportDto> getMatchingActivityReport() {
        try {
            return ResponseEntity.ok(userReportService.generateMatchingReport());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/activity/range")
    public ResponseEntity<MatchingReportDto> getMatchingReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(userReportService.generateCustomRangeReport(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // === DIVERSIDAD DE BAILE ===

    @GetMapping("/dance/diversity")
    public ResponseEntity<DanceDiversityReportDto> getDanceDiversityReport() {
        return ResponseEntity.ok(userReportService.generateDanceDiversityReport());
    }

    @GetMapping("/dance/styles")
    public ResponseEntity<List<DanceStyleDistributionDto>> getDanceStyles() {
        DanceDiversityReportDto report = userReportService.generateDanceDiversityReport();
        return ResponseEntity.ok(report.getStyleDistribution());
    }

    @GetMapping("/dance/combinations")
    public ResponseEntity<List<StyleCombinationDto>> getStyleCombinations(
            @RequestParam(defaultValue = "10") int limit) {
        DanceDiversityReportDto report = userReportService.generateDanceDiversityReport();
        return ResponseEntity.ok(report.getTopCombinations().stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList()));
    }

    // === MÉTRICAS RÁPIDAS PARA DECISIONES ===

    @GetMapping("/quick/dance")
    public ResponseEntity<Map<String, Object>> getQuickDanceMetrics() {
        DanceDiversityReportDto report = userReportService.generateDanceDiversityReport();
        Map<String, Object> quickMetrics = new java.util.HashMap<>();
        quickMetrics.put("totalActiveDancers", report.getTotalActiveDancers());
        quickMetrics.put("averageStylesPerDancer", report.getAverageStylesPerDancer());
        quickMetrics.put("totalStyles", report.getStyleDistribution().size());
        quickMetrics.put("mostPopularStyle", report.getStyleDistribution().isEmpty() ? "N/A" :
                report.getStyleDistribution().get(0).getStyleName());
        quickMetrics.put("topCombination", report.getTopCombinations().isEmpty() ? "N/A" :
                report.getTopCombinations().get(0).getCombination());
        return ResponseEntity.ok(quickMetrics);
    }

    @GetMapping("/quick/geography")
    public ResponseEntity<Map<String, Object>> getQuickGeographyMetrics() {
        GeographicReportDto report = userReportService.generateEnhancedGeographicReport();
        Map<String, Object> quickMetrics = new java.util.HashMap<>();
        quickMetrics.put("totalUsers", report.getTotalUsers());
        quickMetrics.put("totalDancers", report.getTotalDancers());
        quickMetrics.put("totalCities", report.getTotalCities());
        quickMetrics.put("totalCountries", report.getTotalCountries());
        quickMetrics.put("topCity", report.getTopCities().isEmpty() ? "N/A" :
                report.getTopCities().get(0).getName());
        quickMetrics.put("topCountry", report.getTopCountries().isEmpty() ? "N/A" :
                report.getTopCountries().get(0).getName());
        return ResponseEntity.ok(quickMetrics);
    }

    /**
     * GET /api/reports/general
     * Obtiene un reporte general de todos los eventos e inscripciones
     */
    @GetMapping("/general")
    public ResponseEntity<EventGeneralReportDto> getGeneralReport() {
        try {
            EventGeneralReportDto report = userReportService.getEventGeneralReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/organizers
     * Obtiene reportes de todos los organizadores
     */
    @GetMapping("/organizers")
    public ResponseEntity<List<OrganizerEventReportDto>> getOrganizerReports() {
        try {
            List<OrganizerEventReportDto> reports = userReportService.getOrganizerEventReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/events/{eventId}
     * Obtiene un reporte detallado de un evento específico
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDetailReportDto> getEventDetailReport(@PathVariable Long eventId) {
        try {
            EventDetailReportDto report = userReportService.getEventDetailReport(eventId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/registrations
     * Obtiene un reporte de inscripciones por período
     * Parámetros: startDate (yyyy-MM-dd), endDate (yyyy-MM-dd)
     */
    @GetMapping("/registrations")
    public ResponseEntity<RegistrationReportDto> getRegistrationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            RegistrationReportDto report = userReportService.getRegistrationReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/locations
     * Obtiene reportes de eventos agrupados por ubicación
     */
    @GetMapping("/locations")
    public ResponseEntity<List<LocationEventReportDto>> getLocationReports() {
        try {
            List<LocationEventReportDto> reports = userReportService.getLocationEventReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/financial
     * Obtiene un reporte financiero por período
     * Parámetros: startDate (yyyy-MM-dd), endDate (yyyy-MM-dd)
     */
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            FinancialReportDto report = userReportService.getFinancialReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/upcoming
     * Obtiene un reporte de eventos próximos
     * Parámetro opcional: days (default: 30)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingEventReportDto>> getUpcomingEventsReport(
            @RequestParam(defaultValue = "30") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest().build();
            }
            List<UpcomingEventReportDto> reports = userReportService.getUpcomingEventsReport(days);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/events/{eventId}/participants
     * Obtiene solo la lista de participantes de un evento (endpoint específico)
     */
    @GetMapping("/events/{eventId}/participants")
    public ResponseEntity<List<ParticipantInfoDto>> getEventParticipants(@PathVariable Long eventId) {
        try {
            EventDetailReportDto report = userReportService.getEventDetailReport(eventId);
            return ResponseEntity.ok(report.getParticipants());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/reports/summary
     * Obtiene un resumen rápido para dashboard
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        try {
            EventGeneralReportDto generalReport = userReportService.getEventGeneralReport();
            List<UpcomingEventReportDto> upcomingEvents = userReportService.getUpcomingEventsReport(7);

            DashboardSummaryDto summary = new DashboardSummaryDto();
            summary.setTotalEvents(generalReport.getTotalEvents());
            summary.setTotalRegistrations(generalReport.getTotalRegistrations());
            summary.setTotalRevenue(generalReport.getTotalRevenue());
            summary.setUpcomingEvents(upcomingEvents);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
