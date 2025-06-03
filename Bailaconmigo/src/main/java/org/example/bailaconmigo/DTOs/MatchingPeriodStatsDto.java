package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchingPeriodStatsDto {
    private Long likesGiven;
    private Long matchesCreated;
    private Double conversionRate;
    private Long activeUsers; // usuarios que dieron al menos 1 like
}