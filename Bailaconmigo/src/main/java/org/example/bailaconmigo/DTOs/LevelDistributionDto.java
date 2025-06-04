package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bailaconmigo.Entities.Enum.Level;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelDistributionDto {
    private Level level;
    private String levelName;
    private Long count;
    private Double percentage;
}
