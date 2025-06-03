package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchingReportDto {

    // Métricas básicas generales
    private Long totalLikesGiven;
    private Long totalSuccessfulMatches;
    private Double conversionRate; // matches/likes
    private Double averageMatchesPerUser;

    // Métricas por período de tiempo
    private MatchingPeriodStatsDto todayStats;
    private MatchingPeriodStatsDto thisWeekStats;
    private MatchingPeriodStatsDto thisMonthStats;

    // Top usuarios más activos (opcional)
    private List<UserMatchingStatsDto> topActiveUsers;
}

