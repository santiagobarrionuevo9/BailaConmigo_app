package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanceDiversityReportDto {
    private Long totalActiveDancers;
    private Double averageStylesPerDancer;
    private List<DanceStyleDistributionDto> styleDistribution;
    private List<LevelDistributionDto> levelDistribution;
    private List<StyleCombinationDto> topCombinations;
    private List<RegionalDanceStyleDto> stylesByRegion;
    private List<StyleLevelStatsDto> styleLevelStats;
}