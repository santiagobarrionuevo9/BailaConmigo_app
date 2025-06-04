package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeographicReportDto {
    private Long totalUsers;
    private Long totalDancers;
    private Integer totalCities;
    private Integer totalCountries;
    private List<EnhancedGeographicDto> topCities;
    private List<EnhancedGeographicDto> topCountries;
    private List<CityStyleRankingDto> cityStyleRankings;
}
