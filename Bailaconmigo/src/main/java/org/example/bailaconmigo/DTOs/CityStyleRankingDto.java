package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityStyleRankingDto {
    private DanceStyle style;
    private String styleName;
    private List<GeographicDto> topCities;
}
