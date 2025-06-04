package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.DanceStyle;
import org.example.bailaconmigo.Entities.Enum.Level;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StyleLevelStatsDto {
    private Level level;
    private String levelName;
    private DanceStyle style;
    private String styleName;
    private Long count;
    private Double percentage;
}
